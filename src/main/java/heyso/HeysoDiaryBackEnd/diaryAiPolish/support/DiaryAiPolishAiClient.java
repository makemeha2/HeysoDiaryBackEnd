package heyso.HeysoDiaryBackEnd.diaryAiPolish.support;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallOptions;
import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallResult;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAiPolishAiClient {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int MAX_OUTPUT_TOKENS = 2500;

    private final AiCallExecutor aiCallExecutor;
    private final DiaryAiPolishPromptFactory promptFactory;

    public AiCallResult polish(String originalContent) {
        List<RoleMessage> messages = List.of(
                new RoleMessage("system", promptFactory.buildSystemPrompt()),
                new RoleMessage("user", promptFactory.buildUserPrompt(originalContent)));

        try {
            AiCallResult result = aiCallExecutor.call(
                    DEFAULT_MODEL,
                    messages,
                    AiCallOptions.of(0.2, null, MAX_OUTPUT_TOKENS));

            String polished = sanitizeResponse(result.content());
            if (StringUtils.isBlank(polished)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned empty polished content");
            }

            return new AiCallResult(
                    polished,
                    result.requestId(),
                    result.promptTokens(),
                    result.completionTokens(),
                    result.totalTokens());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI polish request failed", e);
        }
    }

    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }

    private String sanitizeResponse(String content) {
        if (content == null) {
            return null;
        }

        String sanitized = content.trim();
        if (sanitized.startsWith("```") && sanitized.endsWith("```")) {
            sanitized = sanitized.replaceFirst("^```[a-zA-Z0-9_-]*\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }
}
