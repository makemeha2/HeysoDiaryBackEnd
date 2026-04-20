package heyso.HeysoDiaryBackEnd.diaryAiPolish.support;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.config.AppAiProperties;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAiPolishAiClient {

    private static final int MAX_OUTPUT_TOKENS = 2500;

    private final AiCallExecutor aiCallExecutor;
    private final AppAiProperties appAiProperties;
    private final DiaryAiPolishPromptFactory promptFactory;

    public AiResponse polish(String originalContent) {
        List<AiMessage> messages = List.of(
                new AiMessage("system", promptFactory.buildSystemPrompt()),
                new AiMessage("user", promptFactory.buildUserPrompt(originalContent)));

        try {
            AiResponse result = aiCallExecutor.call(AiRequest.builder()
                    .provider(appAiProperties.getDefaultProvider())
                    .model(appAiProperties.getDefaultDiaryPolishModel())
                    .messages(messages)
                    .temperature(0.2)
                    .maxTokens(MAX_OUTPUT_TOKENS)
                    .build());

            String polished = sanitizeResponse(result.content());
            if (StringUtils.isBlank(polished)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned empty polished content");
            }

            return new AiResponse(
                    polished,
                    result.provider(),
                    result.model(),
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
        return appAiProperties.getDefaultDiaryPolishModel();
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
