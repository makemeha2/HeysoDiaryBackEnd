package heyso.HeysoDiaryBackEnd.diary.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryNudgeResponse;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryNudgeEventLog;
import heyso.HeysoDiaryBackEnd.utils.DateUtil;
import heyso.HeysoDiaryBackEnd.utils.TextSnippetUtil;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DiaryNudgeService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final int MESSAGE_TEXT_MAX = 255;
    private static final int DIARY_SNIPPET_MAX = 1000;

    private static final String BINDING_DOMAIN = "DIARY_NUDGE";
    private static final String BINDING_FEATURE = "NUDGE";

    private final DiaryMapper diaryMapper;
    private final AiCallExecutor aiCallExecutor;
    private final AiPromptResolver aiPromptResolver;
    private final AiModelResolver aiModelResolver;

    @Async("nudgeExecutor")
    public CompletableFuture<DiaryNudgeResponse> createTodayNudgeAsync(Long userId) {
        String messageKey = String.format("%s-%s", userId, DateUtil.nowKorea());
        DiaryNudgeResponse response = new DiaryNudgeResponse(messageKey, "");

        try {
            LocalDate today = LocalDate.now(KOREA_ZONE);
            Diary diary = diaryMapper.selectLatestDiaryBeforeDate(userId, today);

            if (diary != null) {
                String aiMessage = callNudgeModel(diary);

                String finalText = StringUtils.isNotBlank(aiMessage) ? aiMessage : "";
                response.setMessageText(finalText);

                insertEventLogSafely(userId, today, response);
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(response);
        }

        return CompletableFuture.completedFuture(response);
    }

    private String callNudgeModel(Diary diary) {
        try {
            Map<String, String> variables = buildVariables(diary);
            AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(
                    BINDING_DOMAIN, BINDING_FEATURE, variables);
            AiModelResolver.ResolvedModel resolvedModel = aiModelResolver.resolve(resolution.profile());

            List<AiMessage> messages = List.of(
                    new AiMessage("system", resolution.renderedSystemPrompt()),
                    new AiMessage("user", resolution.renderedUserPrompt()));

            Double temperature = resolution.profile().getTemperature() != null
                    ? resolution.profile().getTemperature().doubleValue()
                    : null;
            Double topP = resolution.profile().getTopP() != null
                    ? resolution.profile().getTopP().doubleValue()
                    : null;
            Integer maxTokens = normalizeMaxTokens(resolution.profile().getMaxTokens());

            AiResponse response = aiCallExecutor.call(AiRequest.builder()
                    .provider(resolvedModel.provider())
                    .model(resolvedModel.model())
                    .messages(messages)
                    .temperature(temperature)
                    .topP(topP)
                    .maxTokens(maxTokens)
                    .build());

            String content = response.content();
            if (StringUtils.isBlank(content)) {
                return null;
            }
            return content.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> buildVariables(Diary diary) {
        String diaryDate = diary.getDiaryDate() == null ? "" : diary.getDiaryDate().toString();
        String title = StringUtils.defaultString(diary.getTitle());
        String snippet = TextSnippetUtil.normalizeAndLimit(diary.getContentMd(), DIARY_SNIPPET_MAX);

        return Map.of(
                "diary_date", diaryDate,
                "title", title,
                "content_snippet", snippet);
    }

    private Integer normalizeMaxTokens(Integer maxTokens) {
        if (maxTokens == null || maxTokens <= 0) {
            return null;
        }
        return maxTokens;
    }

    private void insertEventLogSafely(Long userId, LocalDate localDate, DiaryNudgeResponse response) {
        try {
            DiaryNudgeEventLog log = new DiaryNudgeEventLog();
            log.setUserId(userId);
            log.setLocalDate(localDate);
            log.setEventType("shown");
            log.setMessageKey(response.getMessageKey());
            log.setMessageText(truncate(response.getMessageText(), MESSAGE_TEXT_MAX));
            diaryMapper.insertDiaryNudgeEventLog(log);
        } catch (Exception ignored) {
            // 로그 실패는 무시
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
