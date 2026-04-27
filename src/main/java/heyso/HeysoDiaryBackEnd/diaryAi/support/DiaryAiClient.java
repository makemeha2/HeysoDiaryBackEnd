package heyso.HeysoDiaryBackEnd.diaryAi.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplate;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.comCd.mapper.CommonCodeMapper;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;
import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import heyso.HeysoDiaryBackEnd.mypage.model.UserAIFeedbackSetting;
import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import lombok.RequiredArgsConstructor;

/**
 * 일기 AI 멘토 댓글 생성을 담당하는 AI 클라이언트.
 *
 * <pre>
 * 전체 흐름은 두 단계로 분리된다.
 *
 *   [1단계 - 프롬프트 해석: resolve()]
 *     ① buildVariables()  — 사용자 설정을 {{변수}} 맵으로 변환
 *     ② AiPromptResolver  — 바인딩(DIARY_AI / COMMENT)으로 DB 템플릿 조회
 *                           → {{ > DIARY_AI_USER_PROFILE_BLOCK }} include 치환
 *                           → {{ > DIARY_AI_MBTI_GUIDE }}         include 치환
 *                           → 나머지 {{변수}} 치환
 *     ③ AiModelResolver   — 런타임 프로파일에서 실제 모델명·provider 확인
 *     ④ DiaryAiResolution 반환 (렌더된 프롬프트 + 프로파일 + 모델 정보)
 *
 *   [2단계 - AI 호출: execute()]
 *     ① DiaryAiResolution 에서 system / user 메시지 구성
 *     ② AiCallExecutor 를 통해 OpenAI(또는 Claude) API 호출
 *     ③ 응답 검증 후 AiResponse 반환
 *
 * 두 단계를 분리한 이유:
 *   서비스 레이어가 1단계 결과(렌더된 프롬프트·모델)를 이용해
 *   AI 호출 전에 tb_diary_ai_run 실행 기록을 저장하기 위함이다.
 *   AI 호출이 실패해도 어떤 프롬프트로 시도했는지 추적할 수 있다.
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DiaryAiClient {

    // tb_mst_ai_prompt_binding 에서 조회할 도메인 타입 · 기능 키
    private static final String BINDING_DOMAIN   = "DIARY_AI";
    private static final String FEATURE_COMMENT  = "COMMENT";

    // tb_user_ai_feedback_setting 이 없거나 값이 비어 있을 때 사용하는 기본 코드값
    // (V6 마이그레이션 DB 컬럼 DEFAULT 와 일치시킨다)
    private static final String DEFAULT_SPEECH_TONE  = "POLITE";
    private static final String DEFAULT_STYLE        = "BALANCED";
    private static final String DEFAULT_INTENSITY    = "NORMAL";
    private static final String DEFAULT_QUESTION     = "ASK";
    private static final String DEFAULT_LENGTH       = "MEDIUM";
    private static final String DEFAULT_LANG_MODE    = "FOLLOW_DIARY";

    private final AiPromptResolver       aiPromptResolver;
    private final AiModelResolver        aiModelResolver;
    private final AiCallExecutor         aiCallExecutor;
    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final CommonCodeMapper       commonCodeMapper;
    private final MonitoringEventService monitoringEventService;

    /**
     * 공통코드 라벨 캐시.
     * groupId → { codeId → CommonCode } 구조로 애플리케이션 재기동 시 갱신된다.
     * 공통코드 변경은 재기동이 필요하다는 운영 규약이 이 캐시의 전제다.
     */
    private final Map<String, Map<String, CommonCode>> codeCache = new ConcurrentHashMap<>();

    // =========================================================================
    // 1단계: 프롬프트 해석
    // =========================================================================

    /**
     * 사용자 설정과 일기 데이터를 바탕으로 AI 프롬프트를 완성한다.
     *
     * <pre>
     * 처리 순서:
     *   1. buildVariables()  — 입력 데이터를 {{변수}} 맵으로 변환
     *   2. AiPromptResolver  — DB 템플릿(DIARY_AI / COMMENT 바인딩)에 변수 치환
     *                          ┗ include 문법 {{ > KEY }} 는 AiTemplateRenderer 가 재귀 처리
     *   3. AiModelResolver   — 런타임 프로파일의 model 코드값 → 실제 모델명·provider 변환
     * </pre>
     *
     * @param input 일기 데이터 + 사용자 설정
     * @return 렌더된 프롬프트 + 프로파일 + 모델 정보 (2단계 execute 에 전달)
     */
    public DiaryAiResolution resolve(DiaryAiCallInput input) {
        // 사용자 설정 → {{변수}} 맵 변환 (프롬프트 템플릿의 자리표시자를 채울 값들)
        Map<String, String> variables = buildVariables(input);
        try {
            // AiPromptResolver 가 바인딩 조회 → 시스템/유저 템플릿 렌더링 수행
            AiPromptResolver.BindingResolution bindingResolution =
                    aiPromptResolver.resolve(BINDING_DOMAIN, FEATURE_COMMENT, variables);
            // 런타임 프로파일의 model 코드값을 실제 모델명·provider 로 해석
            AiModelResolver.ResolvedModel resolvedModel =
                    aiModelResolver.resolve(bindingResolution.profile());
            return new DiaryAiResolution(
                    bindingResolution.renderedSystemPrompt(),
                    bindingResolution.renderedUserPrompt(),
                bindingResolution.profile(),
                    resolvedModel);
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "DIARY_AI prompt resolution failed", e, null);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI prompt resolution failed");
        }
    }

    // =========================================================================
    // 2단계: AI 호출
// =========================================================================

    /**
     * 해석된 프롬프트로 AI API 를 호출하고 응답을 반환한다.
     *
     * <pre>
     * 처리 순서:
     *   1. 프로파일에서 temperature · topP · maxTokens 추출
     *   2. system / user 메시지 목록 구성
     *   3. AiCallExecutor 를 통해 provider 별 클라이언트(OpenAI 등)로 API 호출
     *   4. 응답 본문 검증 후 반환
     * </pre>
     *
     * @param resolution resolve() 가 반환한 해석 결과
     * @return AI 응답 (content · requestId · 토큰 수 등)
     */
    public AiResponse execute(DiaryAiResolution resolution) {
        AiRuntimeProfile profile = resolution.profile();
        // 런타임 프로파일에 설정된 파라미터 적용 (null 이면 API 기본값 사용)
        Double  temperature = profile.getTemperature() != null ? profile.getTemperature().doubleValue() : null;
        Double  topP        = profile.getTopP()         != null ? profile.getTopP().doubleValue()        : null;
        Integer maxTokens   = normalizeMaxTokens(profile.getMaxTokens());

        // system 역할: 멘토 행동 지침 + 사용자 설정 + MBTI 가이드 + 과거 일기 컨텍스트
        // user  역할: 오늘 일기 본문 + 댓글 작성 지시
        List<AiMessage> messages = List.of(
                new AiMessage("system", resolution.renderedSystemPrompt()),
                new AiMessage("user",   resolution.renderedUserPrompt()));

        try {
            AiResponse result = aiCallExecutor.call(AiRequest.builder()
                    .provider(resolution.resolvedModel().provider())
                    .model(resolution.resolvedModel().model())
                    .messages(messages)
                    .temperature(temperature)
                    .topP(topP)
                    .maxTokens(maxTokens)
                    .build());

            String content = result.content();
            if (StringUtils.isBlank(content)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned empty content");
            }

            return new AiResponse(
                    content.trim(),
                    result.provider(),
                    result.model(),
                    result.requestId(),
                    result.promptTokens(),
                    result.completionTokens(),
                    result.totalTokens());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "DIARY_AI call failed", e, null);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI request failed");
        }
    }

    // =========================================================================
    // 프롬프트 변수 빌드
    // =========================================================================

    /**
     * 사용자 설정과 일기 데이터를 AI 템플릿의 {{변수}} 맵으로 변환한다.
     *
     * <pre>
     * 변수 목록과 대응 템플릿 자리표시자:
     *
     *   [사용자 프로필 - DIARY_AI_USER_PROFILE_BLOCK 내부]
     *     nickname             ← tb_user_profile.nickname
     *     speech_tone_label    ← AIFB_SPEECH_TONE 코드명 (예: "정중하게")
     *     feedback_style_label ← AIFB_STYLE 코드명       (예: "공감")
     *     intensity_label      ← AIFB_INTENSITY 코드명   (예: "보통")
     *     question_label       ← AIFB_QUESTION 코드명    (예: "있음")
     *     length_label         ← AIFB_LENGTH 코드명 + extra_info1 (예: "보통(400자 내외)")
     *     lang_mode_label      ← AIFB_LANG_MODE 코드명  (예: "일기 언어 따라가기")
     *     lang_fixed_suffix    ← 고정 언어 부가 문구     (예: " (고정: ko)")
     *
     *   [MBTI 가이드 - DIARY_AI_MBTI_GUIDE 내부]
     *     mbti_guideline       ← MBTI_GUIDE_{MBTI유형} 템플릿 본문 (예: MBTI_GUIDE_INFP)
     *                           MBTI 미설정 시 빈 문자열 → AI 가 MBTI 섹션을 무시함
     *
     *   [일기 데이터 - 시스템/유저 프롬프트 직접 사용]
     *     diary_date       ← 오늘 일기 날짜
     *     title            ← 오늘 일기 제목
     *     content_snippet  ← 오늘 일기 본문 (길이 제한 적용)
     *     context_block    ← 과거 일기 컨텍스트 블록 (시스템 프롬프트 하단)
     * </pre>
     *
     * 우선순위 규칙: tb_user_ai_feedback_setting 설정 > MBTI 가이드 > 일기 내용 자체
     */
    private Map<String, String> buildVariables(DiaryAiCallInput input) {
        UserProfile           profile = input.userProfile();
        UserAIFeedbackSetting setting = input.feedbackSetting();

        // 닉네임: 미설정 또는 프로필 없음이면 "익명" 표시
        String nickname = (profile != null && StringUtils.isNotBlank(profile.getNickname()))
                ? profile.getNickname() : "익명";
        // MBTI: null 이면 resolveMbtiGuideline() 이 빈 문자열 반환
        String mbti = profile != null ? profile.getMbti() : null;

        // 피드백 설정 코드값 결정 (레코드 없거나 값이 비면 DB DEFAULT 와 동일한 상수 사용)
        String speechToneCd    = coalesce(setting != null ? setting.getSpeechToneCd()    : null, DEFAULT_SPEECH_TONE);
        String feedbackStyleCd = coalesce(setting != null ? setting.getFeedbackStyleCd() : null, DEFAULT_STYLE);
        String intensityCd     = coalesce(setting != null ? setting.getIntensityCd()     : null, DEFAULT_INTENSITY);
        String questionCd      = coalesce(setting != null ? setting.getQuestionCd()      : null, DEFAULT_QUESTION);
        String lengthCd        = coalesce(setting != null ? setting.getLengthCd()        : null, DEFAULT_LENGTH);
        String langModeCd      = coalesce(setting != null ? setting.getLangModeCd()      : null, DEFAULT_LANG_MODE);

        // 최종 변수 맵 조립 — 이 맵이 DB 템플릿의 {{key}} 자리에 치환됨
        Map<String, String> vars = new HashMap<>();
        vars.put("nickname",             nickname);
        vars.put("speech_tone_label",    resolveLabel("AIFB_SPEECH_TONE", speechToneCd));
        vars.put("feedback_style_label", resolveLabel("AIFB_STYLE",       feedbackStyleCd));
        vars.put("intensity_label",      resolveLabel("AIFB_INTENSITY",   intensityCd));
        vars.put("question_label",       resolveLabel("AIFB_QUESTION",    questionCd));
        vars.put("length_label",         resolveLengthLabel(lengthCd));    // 글자수 포함
        vars.put("lang_mode_label",      resolveLabel("AIFB_LANG_MODE",   langModeCd));
        vars.put("lang_fixed_suffix",    buildLangFixedSuffix(langModeCd, setting));
        vars.put("mbti_guideline",       resolveMbtiGuideline(mbti));      // MBTI_GUIDE_* 템플릿 내용
        vars.put("diary_date",           StringUtils.defaultString(input.diaryDate()));
        vars.put("title",                StringUtils.defaultString(input.diaryTitle()));
        vars.put("content_snippet",      StringUtils.defaultString(input.diaryContent()));
        vars.put("context_block",        StringUtils.defaultString(input.contextBlock()));
        return vars;
    }

    // =========================================================================
    // 변수 값 해석 유틸
    // =========================================================================

    /** null 또는 blank 이면 기본값 반환 */
    private String coalesce(String value, String defaultValue) {
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * 공통코드 그룹에서 코드명(code_name)을 조회한다.
     * 코드가 없으면 codeId 를 그대로 반환해 프롬프트가 깨지지 않도록 방어한다.
     */
    private String resolveLabel(String groupId, String codeId) {
        CommonCode code = getCachedCode(groupId, codeId);
        return code != null ? code.getCodeName() : codeId;
    }

    /**
     * 응답 길이 라벨을 "코드명(글자수 기준)" 형태로 조합한다.
     * extra_info1 에 글자수 기준이 없으면 코드명만 사용한다.
     * 예) "보통(400자 내외)"
     */
    private String resolveLengthLabel(String lengthCd) {
        CommonCode code = getCachedCode("AIFB_LENGTH", lengthCd);
        if (code == null) return lengthCd;
        return StringUtils.isNotBlank(code.getExtraInfo1())
                ? code.getCodeName() + "(" + code.getExtraInfo1() + ")"
                : code.getCodeName();
    }

    /**
     * 언어 고정 모드일 때 부가 문구를 반환한다.
     * 예) lang_mode_cd=FIXED, fixed_lang=ko → " (고정: ko)"
     * 일기 언어 따라가기 모드이면 빈 문자열을 반환해 프롬프트에서 자연스럽게 생략된다.
     */
    private String buildLangFixedSuffix(String langModeCd, UserAIFeedbackSetting setting) {
        if ("FIXED".equals(langModeCd) && setting != null && StringUtils.isNotBlank(setting.getFixedLang())) {
            return " (고정: " + setting.getFixedLang() + ")";
        }
        return "";
    }

    /**
     * 사용자 MBTI 에 해당하는 피드백 가이드라인 본문을 가져온다.
     *
     * <pre>
     * 조회 키: "MBTI_GUIDE_{MBTI유형}" (예: MBTI_GUIDE_INFP)
     * 해당 템플릿이 없거나 비활성이면 빈 문자열을 반환한다.
     * → 시스템 프롬프트의 "## MBTI 참고 가이드" 섹션이 비어 있는 상태가 되며,
     *   AI 는 MBTI 일반화 없이 일기 내용 자체에만 집중하게 된다.
     *
     * MBTI 가이드라인은 tb_mst_ai_prompt_template 의 MBTI_GUIDE_* CHILD 템플릿에 관리되며,
     * 운영자가 관리 UI 에서 각 유형별 지침을 독립적으로 수정할 수 있다.
     * </pre>
     */
    private String resolveMbtiGuideline(String mbti) {
        if (StringUtils.isBlank(mbti)) return "";
        String key = "MBTI_GUIDE_" + mbti.toUpperCase().trim();
        AiPromptTemplate t = aiPromptTemplateMapper.selectByKey(key);
        if (t == null || !Integer.valueOf(1).equals(t.getIsActive())) return "";
        return StringUtils.defaultString(t.getContent()).trim();
    }

    /**
     * 공통코드를 그룹 단위로 캐싱하여 반환한다.
     * 첫 조회 시 해당 그룹의 전체 활성 코드를 DB 에서 로드하고, 이후 요청은 캐시에서 처리한다.
     * 공통코드 변경 시 애플리케이션 재기동이 필요하다.
     */
    private CommonCode getCachedCode(String groupId, String codeId) {
        return codeCache
                .computeIfAbsent(groupId, gid -> {
                    Map<String, CommonCode> m = new HashMap<>();
                    for (CommonCode c : commonCodeMapper.selectActiveCommonCodeListByGroupId(gid)) {
                        m.put(c.getCodeId(), c);
                    }
                    return m;
                })
                .get(codeId);
    }

    /** maxTokens 가 null 이거나 0 이하이면 null 로 정규화한다 (API 기본값 사용). */
    private Integer normalizeMaxTokens(Integer maxTokens) {
        if (maxTokens == null || maxTokens <= 0) return null;
        return maxTokens;
    }
}
