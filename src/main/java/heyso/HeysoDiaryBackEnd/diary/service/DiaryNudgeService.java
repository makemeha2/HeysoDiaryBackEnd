package heyso.HeysoDiaryBackEnd.diary.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;
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
    private static final String DEFAULT_MODEL = "gpt-4o";

    private static final String NUDGE_SYSTEM_PROMPT = """
            너는 사용자의 일기 내용을 바탕으로 오늘 하루를 정리하는 일기를 쓸수 있게 도와주는 안부 질문을 만든다.
            - 1~2문장, 한국어
            - 출력은 '메시지 텍스트'만 반환할 것 (마크다운 불필요)
            - 사용자가 메세지를 읽고 일기 작성을 하고 싶어지도록 노굴적이거나 직접적이지 않은 메세지도 포함시켜도 된다.
            """;

    private final DiaryMapper diaryMapper;
    private final OpenAiClient openAiClient;

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
        String promptUser = buildUserPrompt(diary);

        List<RoleMessage> messages = new ArrayList<>();
        messages.add(new RoleMessage("developer", NUDGE_SYSTEM_PROMPT));
        messages.add(new RoleMessage("user", promptUser));

        CallResponseSpec responseSpec;
        try {
            responseSpec = openAiClient.createResponseSpec(
                    DEFAULT_MODEL,
                    messages,
                    null,
                    null,
                    null);
        } catch (Exception e) {
            return null;
        }

        String content = responseSpec.content();
        if (StringUtils.isBlank(content)) {
            return null;
        }
        return content.trim();
    }

    private String buildUserPrompt(Diary diary) {
        String diaryDate = diary.getDiaryDate() == null ? "" : diary.getDiaryDate().toString();
        String title = StringUtils.defaultString(diary.getTitle());
        String snippet = TextSnippetUtil.normalizeAndLimit(diary.getContentMd(), DIARY_SNIPPET_MAX);

        return """
                [오늘 이전 최신 일기]
                날짜: %s
                제목: %s
                내용:
                %s

                위 내용을 바탕으로 짧게 안부로 말을 건낼 수 있는 질문을 던져줘.
                """.formatted(diaryDate, title, snippet);
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
