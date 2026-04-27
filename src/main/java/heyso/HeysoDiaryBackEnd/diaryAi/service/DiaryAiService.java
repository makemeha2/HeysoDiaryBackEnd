package heyso.HeysoDiaryBackEnd.diaryAi.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentCreateRequest;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentCreateResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentListItemResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiFeedbackCreateRequest;
import heyso.HeysoDiaryBackEnd.diaryAi.mapper.DiaryAiMapper;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiComment;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiFeedback;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRun;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRunContext;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiRunStatus;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiSourceType;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiTriggerType;
import heyso.HeysoDiaryBackEnd.diaryAi.support.DiaryAiCallInput;
import heyso.HeysoDiaryBackEnd.diaryAi.support.DiaryAiClient;
import heyso.HeysoDiaryBackEnd.diaryAi.support.DiaryAiResolution;
import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.mypage.mapper.UserAIFeedbackSettingMapper;
import heyso.HeysoDiaryBackEnd.mypage.mapper.UserProfileMapper;
import heyso.HeysoDiaryBackEnd.mypage.model.UserAIFeedbackSetting;
import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.utils.TextSnippetUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryAiService {

    private final DiaryAiClient diaryAiClient;
    private final DiaryMapper diaryMapper;
    private final DiaryAiMapper diaryAiMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAIFeedbackSettingMapper userAIFeedbackSettingMapper;

    // 컨텍스트 수집 기본 한도 (최근 일기 / 태그 연관 일기 / 전체 합산)
    private static final int DEFAULT_RECENT_LIMIT = 3;
    private static final int DEFAULT_TAG_LIMIT = 5;
    private static final int DEFAULT_TOTAL_CONTEXT_LIMIT = 20;
    private static final int DEFAULT_COMMENT_LIST_LIMIT = 10;

    // 프롬프트에 삽입할 텍스트 길이 상한 (토큰 비용·품질 균형)
    private static final int TODAY_DIARY_SNIPPET_MAX = 1_600; // 오늘 일기 본문
    private static final int CONTEXT_DIARY_SNIPPET_MAX = 500; // 과거 일기 1건당 스니펫
    private static final int CONTEXT_BLOCK_MAX = 6_000; // 컨텍스트 블록 전체

    // =========================================================================
    // AI 댓글 생성
    // =========================================================================

    /** 기본 파라미터로 AI 댓글을 생성한다 (버튼 트리거). */
    @Transactional
    public DiaryAiCommentCreateResponse createAiComment(Long diaryId) {
        DiaryAiCommentCreateRequest request = new DiaryAiCommentCreateRequest();
        request.setRecentLimit(DEFAULT_RECENT_LIMIT);
        request.setTagLimit(DEFAULT_TAG_LIMIT);
        return createAiComment(diaryId, request);
    }

    /**
     * AI 멘토 댓글을 생성하고 저장한다.
     *
     * <pre>
     * 전체 처리 흐름:
     *
     *   [준비 단계]
     *   2. 과거 일기 수집 (최근 + 태그 연관)
     *   3. 컨텍스트 블록 문자열 조립 (buildContextBlock)
     *   4. 사용자 프로필·AI 피드백 설정 조회 (개인화 입력 데이터)
     *
     *   [1단계 - 프롬프트 해석 (AI 호출 전 감사 데이터 확정)]
     *   5. DiaryAiClient.resolve() 호출
     *      → 사용자 설정을 변수 맵으로 변환 후 DB 템플릿에 치환
     *      → 렌더된 시스템/유저 프롬프트 + 모델 정보 확보
     *   6. 확정된 프롬프트·모델 정보로 tb_diary_ai_run 실행 기록 선행 저장 (status=RUNNING)
     *      → AI 호출 실패 시에도 어떤 프롬프트로 시도했는지 추적 가능
     *
     *   [2단계 - AI 호출]
     *   7. DiaryAiClient.execute() 로 AI API 호출
     *   8. 댓글 저장(tb_diary_ai_comment) + 실행 기록 SUCCESS 업데이트
     *
     *   [예외 처리]
     *   - AI 호출 실패 시 tb_diary_ai_run 을 ERROR 로 마감하고 예외 재전파
     * </pre>
     */
    @Transactional
    public DiaryAiCommentCreateResponse createAiComment(Long diaryId, DiaryAiCommentCreateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!user.getUserId().equals(diary.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this diary");
        }

        // 2. 컨텍스트 수집 파라미터 정규화
        int recentLimit = Objects.requireNonNullElse(request.getRecentLimit(), DEFAULT_RECENT_LIMIT);
        int tagLimit = Objects.requireNonNullElse(request.getTagLimit(), DEFAULT_TAG_LIMIT);
        int totalContextLimit = DEFAULT_TOTAL_CONTEXT_LIMIT;

        List<String> tagNames = diaryMapper.selectTagNamesByDiaryId(diaryId);

        List<DiarySummary> recentDiaries = diaryMapper.selectRecentDiaries(user.getUserId(), diaryId, recentLimit);
        List<DiarySummary> tagDiaries = tagNames == null || tagNames.isEmpty()
                ? List.of()
                : diaryMapper.selectDiariesByTags(user.getUserId(), diaryId, tagNames, tagLimit);

        // 3. 중복 제거 후 컨텍스트 목록 조합, 시스템 프롬프트 {{context_block}} 에 삽입할 문자열 생성
        List<DiaryAiRunContext> contexts = buildContexts(recentDiaries, tagDiaries, totalContextLimit);
        String contextBlock = buildContextBlock(contexts, recentDiaries, tagDiaries);

        // 4. 개인화 데이터 조회 (없으면 DiaryAiClient 가 기본값으로 처리)
        UserProfile userProfile = userProfileMapper.selectUserProfileByUserId(user.getUserId());
        UserAIFeedbackSetting feedbackSetting = userAIFeedbackSettingMapper
                .selectUserAIFeedbackSettingByUserId(user.getUserId());

        // 오늘 일기 데이터 준비 (유저 프롬프트 변수로 사용)
        String diaryDate = diary.getDiaryDate() == null ? "" : diary.getDiaryDate().toString();
        String diaryTitle = StringUtils.defaultString(diary.getTitle());
        String diaryContent = TextSnippetUtil.normalizeAndLimit(diary.getContentMd(), TODAY_DIARY_SNIPPET_MAX);

        // 5. [1단계] 프롬프트 해석 — AI 호출 없이 렌더된 프롬프트·모델 정보만 확보
        DiaryAiCallInput callInput = new DiaryAiCallInput(diaryDate, diaryTitle, diaryContent, contextBlock,
                userProfile, feedbackSetting);
        DiaryAiResolution resolution = diaryAiClient.resolve(callInput);

        // 6. 확정된 프롬프트로 실행 기록 선행 저장 (AI 호출 전에 저장해야 실패 추적이 가능)
        String promptHash = sha256(resolution.renderedSystemPrompt() + "\n---\n" + resolution.renderedUserPrompt());

        DiaryAiRun run = DiaryAiRun.builder()
                .diaryId(diaryId)
                .userId(user.getUserId())
                .triggerType(DiaryAiTriggerType.BUTTON)
                .status(DiaryAiRunStatus.RUNNING)
                // 모델·파라미터는 런타임 프로파일에서 결정됨 (요청 DTO 의 model/temperature 는 무시)
                .model(resolution.resolvedModel().model())
                .temperature(resolution.profile().getTemperature() != null
                        ? resolution.profile().getTemperature().doubleValue()
                        : null)
                .topP(resolution.profile().getTopP() != null
                        ? resolution.profile().getTopP().doubleValue()
                        : null)
                .maxOutputTokens(resolution.profile().getMaxTokens())
                .promptSystem(resolution.renderedSystemPrompt())
                .promptUser(resolution.renderedUserPrompt())
                .promptHash(promptHash)
                .diaryUpdatedAtSnapshot(diary.getUpdatedAt())
                .build();

        diaryAiMapper.insertDiaryAiRun(run);

        if (!contexts.isEmpty()) {
            diaryAiMapper.insertDiaryAiRunContextList(run.getRunId(), contexts);
        }

        // 7 & 8. [2단계] AI 호출 → 댓글·실행기록 저장
        try {
            AiResponse aiResult = diaryAiClient.execute(resolution);

            DiaryAiComment comment = DiaryAiComment.builder()
                    .diaryId(diaryId)
                    .userId(user.getUserId())
                    .runId(run.getRunId())
                    .contentMd(aiResult.content())
                    .isPinned(false)
                    .isDeleted(false)
                    .build();

            diaryAiMapper.insertDiaryAiComment(comment);

            diaryAiMapper.updateDiaryAiRunSuccess(
                    run.getRunId(),
                    aiResult.requestId(),
                    aiResult.promptTokens(),
                    aiResult.completionTokens(),
                    aiResult.totalTokens(),
                    run.getCostUsd());

            return DiaryAiCommentCreateResponse.of(
                    run.getRunId(),
                    comment.getAiCommentId(),
                    comment.getContentMd(),
                    comment.getCreatedAt());
        } catch (ResponseStatusException e) {
            // AI 호출 실패 — 실행 기록을 ERROR 로 마감 후 예외 재전파
            diaryAiMapper.updateDiaryAiRunError(run.getRunId(), "LLM_ERROR", safeErrorMessage(e.getReason()));
            throw e;
        } catch (Exception e) {
            diaryAiMapper.updateDiaryAiRunError(run.getRunId(), "UNEXPECTED_ERROR", safeErrorMessage(e.getMessage()));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI comment generation failed");
        }
    }

    // =========================================================================
    // 댓글 목록 조회
    // =========================================================================

    public List<DiaryAiCommentListItemResponse> getAiComments(Long diaryId, Integer limit) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!user.getUserId().equals(diary.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this diary");
        }

        int normalizedLimit = Objects.requireNonNullElse(limit, DEFAULT_COMMENT_LIST_LIMIT);

        List<DiaryAiComment> comments = diaryAiMapper.selectAiCommentsByDiaryId(diaryId, user.getUserId(),
                normalizedLimit);

        List<DiaryAiCommentListItemResponse> responses = new ArrayList<>();
        for (DiaryAiComment comment : comments) {
            responses.add(DiaryAiCommentListItemResponse.of(
                    comment.getAiCommentId(),
                    comment.getDiaryId(),
                    comment.getRunId(),
                    comment.getContentMd(),
                    comment.getIsPinned(),
                    comment.getCreatedAt()));
        }
        return responses;
    }

    // =========================================================================
    // 댓글 피드백
    // =========================================================================

    @Transactional
    public void createFeedback(Long aiCommentId, DiaryAiFeedbackCreateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        // 경로 변수와 요청 본문의 ID 일치 여부 확인
        if (request.getAiCommentId() != null && !aiCommentId.equals(request.getAiCommentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "aiCommentId mismatch");
        }

        DiaryAiComment comment = diaryAiMapper.selectAiCommentById(aiCommentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI comment not found");
        }
        if (!user.getUserId().equals(comment.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot give feedback on this comment");
        }

        DiaryAiFeedback feedback = DiaryAiFeedback.builder()
                .aiCommentId(aiCommentId)
                .userId(user.getUserId())
                .feedbackType(request.getFeedbackType())
                .feedbackReason(trimToEmpty(request.getFeedbackReason(), 255))
                .build();

        try {
            diaryAiMapper.insertDiaryAiFeedback(feedback);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback already exists for this comment");
        }
    }

    // =========================================================================
    // 컨텍스트 블록 조립
    // =========================================================================

    /**
     * 최근 일기·태그 연관 일기를 중복 없이 합산하고 전체 한도에 맞게 자른다.
     * TAG 출처가 RECENT 보다 높은 우선순위를 가진다.
     */
    private List<DiaryAiRunContext> buildContexts(List<DiarySummary> recentDiaries,
            List<DiarySummary> tagDiaries,
            int totalLimit) {
        // TAG 먼저 삽입 → RECENT 로 나머지 채움 (putIfAbsent 로 중복 방지)
        Map<Long, DiaryAiSourceType> orderedSources = new LinkedHashMap<>();

        for (DiarySummary diary : tagDiaries) {
            orderedSources.putIfAbsent(diary.getDiaryId(), DiaryAiSourceType.TAG);
        }
        for (DiarySummary diary : recentDiaries) {
            orderedSources.putIfAbsent(diary.getDiaryId(), DiaryAiSourceType.RECENT);
        }

        List<DiaryAiRunContext> contexts = new ArrayList<>();
        int sortOrder = 1;

        for (Map.Entry<Long, DiaryAiSourceType> entry : orderedSources.entrySet()) {
            if (contexts.size() >= totalLimit) {
                break;
            }
            contexts.add(DiaryAiRunContext.builder()
                    .sourceDiaryId(entry.getKey())
                    .sourceType(entry.getValue())
                    .sortOrder(sortOrder++)
                    .build());
        }

        return contexts;
    }

    /**
     * 컨텍스트 일기들을 시스템 프롬프트에 삽입할 텍스트 블록으로 조립한다.
     * 전체 길이가 CONTEXT_BLOCK_MAX 를 초과하면 그 시점에서 잘라낸다.
     */
    private String buildContextBlock(List<DiaryAiRunContext> contexts,
            List<DiarySummary> recentDiaries,
            List<DiarySummary> tagDiaries) {
        if (contexts.isEmpty()) {
            return "참고할 과거 일기 컨텍스트는 비어 있다.";
        }

        // diaryId → DiarySummary 매핑 (컨텍스트 항목에서 본문을 찾기 위함)
        Map<Long, DiarySummary> diaryMap = new LinkedHashMap<>();
        for (DiarySummary diary : recentDiaries) {
            diaryMap.putIfAbsent(diary.getDiaryId(), diary);
        }
        for (DiarySummary diary : tagDiaries) {
            diaryMap.putIfAbsent(diary.getDiaryId(), diary);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("아래는 과거 일기에서 발췌한 짧은 컨텍스트다.\n");
        sb.append("- 원문을 그대로 길게 인용하지 말고, 맥락 파악에만 사용하라.\n\n");

        for (DiaryAiRunContext ctx : contexts) {
            DiarySummary diary = diaryMap.get(ctx.getSourceDiaryId());
            if (diary == null) {
                continue;
            }

            String block = formatContextDiary(diary, ctx.getSortOrder(), ctx.getSourceType());
            // 전체 블록 길이 초과 시 조기 종료
            if (sb.length() + block.length() > CONTEXT_BLOCK_MAX) {
                break;
            }
            sb.append(block);
        }

        return sb.toString().trim();
    }

    /** 과거 일기 1건을 "(순서) [출처유형] 날짜 - 제목\n본문스니펫" 형식으로 포맷한다. */
    private String formatContextDiary(DiarySummary diary, int sortOrder, DiaryAiSourceType sourceType) {
        LocalDate date = diary.getDiaryDate();
        String dateText = date == null ? "" : date.toString();
        String title = StringUtils.defaultString(diary.getTitle());
        String snippet = TextSnippetUtil.normalizeAndLimit(diary.getContentMd(), CONTEXT_DIARY_SNIPPET_MAX);

        return """
                (%d) [%s] %s - %s
                %s

                """.formatted(sortOrder, sourceType.name(), dateText, title, snippet);
    }

    // =========================================================================
    // 유틸
    // =========================================================================

    /** 시스템/유저 프롬프트를 합산한 SHA-256 해시 — 프롬프트 변경 여부 감지용. */
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** 오류 메시지를 DB 컬럼 최대 길이(2,000자)에 맞게 자른다. */
    private String safeErrorMessage(String message) {
        return trimToEmpty(message, 2_000);
    }

    private String trimToEmpty(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen);
    }
}
