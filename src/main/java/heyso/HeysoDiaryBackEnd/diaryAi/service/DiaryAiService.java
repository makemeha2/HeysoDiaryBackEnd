package heyso.HeysoDiaryBackEnd.diaryAi.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAi.mapper.DiaryAiMapper;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentCreateRequest;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentCreateResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentListItemResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiFeedbackCreateRequest;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiComment;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiFeedback;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRun;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRunContext;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiRunStatus;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiSourceType;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiTriggerType;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryAiService {

    private static final int DEFAULT_RECENT_LIMIT = 10;
    private static final int DEFAULT_TAG_LIMIT = 10;
    private static final int DEFAULT_TOTAL_CONTEXT_LIMIT = 20;
    private static final int DEFAULT_COMMENT_LIST_LIMIT = 10;

    private static final int TODAY_DIARY_SNIPPET_MAX = 1_600;
    private static final int CONTEXT_DIARY_SNIPPET_MAX = 500;
    private static final int CONTEXT_BLOCK_MAX = 6_000;

    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    // private static final String MENTOR_SYSTEM_PROMPT = """
    // 너는 사용자의 일기를 읽고 따뜻하고 성실한 멘토처럼 댓글을 남기는 AI다.
    // - 공감 → 관찰 → 제안 순서로, 짧지만 밀도 있게 작성하라.
    // - 사실을 지어내지 말고, 주어진 정보에 근거해라.
    // - 위험하거나 민감한 조언(의학/법률/투자)은 피하고, 필요 시 전문가 상담을 권유하라.
    // - 비난하거나 단정하지 말고, 선택지를 제시하는 어조를 유지하라.
    // - 출력은 Markdown으로 작성하라.
    // """;
    private static final String MENTOR_SYSTEM_PROMPT = """
            너는 사용자의 일기를 읽고 따뜻하고 성실한 멘토처럼 댓글을 남기는 AI다.
            - 공감 → 관찰 → 제안 순서로, 짧지만 밀도 있게 작성하라.
            - 사실을 지어내지 말고, 주어진 정보에 근거해라.
            - 비난하거나 단정하지 말고, 선택지를 제시하는 어조를 유지하라.
            """;

    private final DiaryMapper diaryMapper;
    private final DiaryAiMapper diaryAiMapper;
    private final OpenAiClient openAiClient;

    @Transactional
    public DiaryAiCommentCreateResponse createAiComment(Long diaryId, DiaryAiCommentCreateRequest request) {
        // 인증 사용자 확인 및 일기 접근 권한 검증
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!user.getUserId().equals(diary.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this diary");
        }

        // 컨텍스트 구성 파라미터 정규화
        int recentLimit = normalizeLimit(request.getRecentLimit(), DEFAULT_RECENT_LIMIT);
        int tagLimit = normalizeLimit(request.getTagLimit(), DEFAULT_TAG_LIMIT);
        int totalContextLimit = DEFAULT_TOTAL_CONTEXT_LIMIT;

        // 최근 일기 + 태그 연관 일기에서 컨텍스트 후보 수집
        List<String> tagNames = diaryMapper.selectTagNamesByDiaryId(diaryId);

        List<DiarySummary> recentDiaries = diaryMapper.selectRecentDiaries(user.getUserId(), diaryId, recentLimit);
        List<DiarySummary> tagDiaries = tagNames == null || tagNames.isEmpty()
                ? List.of()
                : diaryMapper.selectDiariesByTags(user.getUserId(), diaryId, tagNames, tagLimit);

        List<DiaryAiRunContext> contexts = buildContexts(recentDiaries, tagDiaries, totalContextLimit);

        // 시스템/유저 프롬프트 생성 및 해시 계산
        String contextBlock = buildContextBlock(contexts, recentDiaries, tagDiaries);
        String promptSystem = buildSystemPrompt(contextBlock);
        String promptUser = buildUserPrompt(diary, request);

        String promptHash = sha256(promptSystem + "\n---\n" + promptUser);

        String model = StringUtils.isBlank(request.getModel()) ? DEFAULT_MODEL : request.getModel().trim();

        // 실행 기록 먼저 저장 (실패/성공 모두 추적)
        DiaryAiRun run = DiaryAiRun.builder()
                .diaryId(diaryId)
                .userId(user.getUserId())
                .triggerType(DiaryAiTriggerType.BUTTON)
                .status(DiaryAiRunStatus.RUNNING)
                .model(model)
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .maxOutputTokens(request.getMaxOutputTokens())
                .promptSystem(promptSystem)
                .promptUser(promptUser)
                .promptHash(promptHash)
                .diaryUpdatedAtSnapshot(diary.getUpdatedAt())
                .build();

        diaryAiMapper.insertDiaryAiRun(run);

        if (!contexts.isEmpty()) {
            diaryAiMapper.insertDiaryAiRunContextList(run.getRunId(), contexts);
        }

        try {
            // 모델 호출 후 결과를 댓글로 저장
            AiCallResult aiResult = callMentorModel(model, promptSystem, promptUser, request);

            DiaryAiComment comment = DiaryAiComment.builder()
                    .diaryId(diaryId)
                    .userId(user.getUserId())
                    .runId(run.getRunId())
                    .contentMd(aiResult.content())
                    .isPinned(false)
                    .isDeleted(false)
                    .build();

            diaryAiMapper.insertDiaryAiComment(comment);

            // 실행 결과(토큰/요청ID) 업데이트
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
            // 모델 호출 실패 기록
            diaryAiMapper.updateDiaryAiRunError(run.getRunId(), "LLM_ERROR", safeErrorMessage(e.getReason()));
            throw e;
        } catch (Exception e) {
            // 예기치 못한 오류 기록
            diaryAiMapper.updateDiaryAiRunError(run.getRunId(), "UNEXPECTED_ERROR", safeErrorMessage(e.getMessage()));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI comment generation failed");
        }
    }

    public List<DiaryAiCommentListItemResponse> getAiComments(Long diaryId, Integer limit) {
        // 접근 권한 확인 후 댓글 목록 조회
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!user.getUserId().equals(diary.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this diary");
        }

        int normalizedLimit = normalizeLimit(limit, DEFAULT_COMMENT_LIST_LIMIT);

        List<DiaryAiComment> comments = diaryAiMapper.selectAiCommentsByDiaryId(diaryId, user.getUserId(),
                normalizedLimit);

        // 응답 DTO로 변환
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

    @Transactional
    public void createFeedback(Long aiCommentId, DiaryAiFeedbackCreateRequest request) {
        // 요청 본문과 경로의 ID 일치 여부 검증
        User user = SecurityUtils.getCurrentUserOrThrow();

        if (request.getAiCommentId() != null && !aiCommentId.equals(request.getAiCommentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "aiCommentId mismatch");
        }

        // 댓글 존재/권한 검증
        DiaryAiComment comment = diaryAiMapper.selectAiCommentById(aiCommentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI comment not found");
        }
        if (!user.getUserId().equals(comment.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot give feedback on this comment");
        }

        // 피드백 저장 (중복은 예외 처리)
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

    private AiCallResult callMentorModel(String model,
            String promptSystem,
            String promptUser,
            DiaryAiCommentCreateRequest request) {
        // 개발자/사용자 메시지로 프롬프트 구성
        List<RoleMessage> messages = new ArrayList<>();
        messages.add(new RoleMessage("developer", promptSystem));
        messages.add(new RoleMessage("user", promptUser));

        CallResponseSpec responseSpec;
        try {
            // OpenAI 호출 준비
            responseSpec = openAiClient.createResponseSpec(
                    model,
                    messages,
                    request.getTemperature(),
                    request.getTopP(),
                    request.getMaxOutputTokens());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI request failed: " + e.getMessage());
        }

        ChatResponse chatResponse = responseSpec.chatResponse();
        String content = responseSpec.content();

        // 응답 본문 검증
        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned empty assistant content");
        }

        String requestId = null;
        Integer promptTokens = 0;
        Integer completionTokens = 0;
        Integer totalTokens = 0;

        // 사용량(토큰) 추출
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            String metadataId = chatResponse.getMetadata().getId();
            if (StringUtils.isNotBlank(metadataId)) {
                requestId = metadataId;
            }

            Usage usage = chatResponse.getMetadata().getUsage();
            if (usage != null && !(usage instanceof EmptyUsage)) {
                promptTokens = defaultZero(usage.getPromptTokens());
                completionTokens = defaultZero(usage.getCompletionTokens());
                totalTokens = defaultZero(usage.getTotalTokens());
            }
        }

        return new AiCallResult(content.trim(), requestId, promptTokens, completionTokens, totalTokens);
    }

    private List<DiaryAiRunContext> buildContexts(List<DiarySummary> recentDiaries,
            List<DiarySummary> tagDiaries,
            int totalLimit) {
        // 최근/태그 컨텍스트를 중복 없이 정렬된 맵으로 합친다
        Map<Long, DiaryAiSourceType> orderedSources = new LinkedHashMap<>();

        for (DiarySummary diary : recentDiaries) {
            orderedSources.putIfAbsent(diary.getDiaryId(), DiaryAiSourceType.RECENT);
        }
        for (DiarySummary diary : tagDiaries) {
            orderedSources.putIfAbsent(diary.getDiaryId(), DiaryAiSourceType.TAG);
        }

        List<DiaryAiRunContext> contexts = new ArrayList<>();
        int sortOrder = 1;

        // totalLimit까지 컨텍스트 엔트리 생성
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

    private String buildContextBlock(List<DiaryAiRunContext> contexts,
            List<DiarySummary> recentDiaries,
            List<DiarySummary> tagDiaries) {
        // 컨텍스트가 없으면 짧은 안내 문구 반환
        if (contexts.isEmpty()) {
            return "참고할 과거 일기 컨텍스트는 비어 있다.";
        }

        // diaryId -> DiarySummary 매핑
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

        // 길이 제한을 넘기지 않도록 컨텍스트 블록 누적
        for (DiaryAiRunContext ctx : contexts) {
            DiarySummary diary = diaryMap.get(ctx.getSourceDiaryId());
            if (diary == null) {
                continue;
            }

            String block = formatContextDiary(diary, ctx.getSortOrder(), ctx.getSourceType());
            if (sb.length() + block.length() > CONTEXT_BLOCK_MAX) {
                break;
            }
            sb.append(block);
        }

        return sb.toString().trim();
    }

    private String buildSystemPrompt(String contextBlock) {
        // 시스템 프롬프트 + 컨텍스트 블록 결합
        return MENTOR_SYSTEM_PROMPT + "\n\n[과거 일기 컨텍스트]\n" + contextBlock;
    }

    private String buildUserPrompt(DiarySummary diary, DiaryAiCommentCreateRequest request) {
        // 오늘 일기 내용을 요약해서 유저 프롬프트로 구성
        String diaryDate = diary.getDiaryDate() == null ? "" : diary.getDiaryDate().toString();
        String title = StringUtils.defaultString(diary.getTitle());
        String contentSnippet = limitDiaryContent(diary.getContentMd(), TODAY_DIARY_SNIPPET_MAX);

        return """
                [오늘 일기]
                날짜: %s
                제목: %s
                내용:
                %s

                위 일기를 읽고, 한국어로 따뜻하고 구체적인 멘토 댓글을 작성해줘.
                너무 길지 않게, 하지만 실질적인 도움을 줄 것.
                """.formatted(diaryDate, title, contentSnippet);
    }

    private String formatContextDiary(DiarySummary diary, int sortOrder, DiaryAiSourceType sourceType) {
        // 컨텍스트 한 건을 포맷팅
        LocalDate date = diary.getDiaryDate();
        String dateText = date == null ? "" : date.toString();
        String title = StringUtils.defaultString(diary.getTitle());
        String snippet = limitDiaryContent(diary.getContentMd(), CONTEXT_DIARY_SNIPPET_MAX);

        return """
                (%d) [%s] %s - %s
                %s

                """.formatted(sortOrder, sourceType.name(), dateText, title, snippet);
    }

    private String limitDiaryContent(String contentMd, int maxChars) {
        // 공백 정규화 후 길이 제한
        if (StringUtils.isBlank(contentMd)) {
            return "";
        }

        String normalized = contentMd.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars) + "...";
    }

    private int normalizeLimit(Integer value, int defaultValue) {
        // 음수/0/널 처리
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String sha256(String input) {
        // 프롬프트 해시 생성
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

    private String safeErrorMessage(String message) {
        // 오류 메시지 길이 제한
        return trimToEmpty(message, 2_000);
    }

    private String trimToEmpty(String value, int maxLen) {
        // trim + 최대 길이 제한
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen);
    }

    private record AiCallResult(String content,
            String requestId,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens) {
    }
}
