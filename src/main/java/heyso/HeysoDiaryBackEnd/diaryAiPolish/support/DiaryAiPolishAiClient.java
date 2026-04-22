package heyso.HeysoDiaryBackEnd.diaryAiPolish.support;

import java.util.List;
import java.util.Map;

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
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAiPolishAiClient {

    private static final String BINDING_DOMAIN = "DIARY_AI_POLISH";
    private static final String BINDING_FEATURE = "POLISH";

    private final AiCallExecutor aiCallExecutor;
    private final AiPromptResolver aiPromptResolver;
    private final AiModelResolver aiModelResolver;

    public AiResponse polish(String originalContent) {
        Map<String, String> variables = Map.of("original_content", originalContent);
        AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(BINDING_DOMAIN, BINDING_FEATURE,
                variables);

        AiModelResolver.ResolvedModel resolvedModel = aiModelResolver.resolve(resolution.profile());

        List<AiMessage> messages = List.of(
                new AiMessage("system", resolution.renderedSystemPrompt()),
                new AiMessage("user", resolution.renderedUserPrompt()));

        try {
            Double temperature = resolution.profile().getTemperature() != null
                    ? resolution.profile().getTemperature().doubleValue()
                    : null;
            Double topP = resolution.profile().getTopP() != null
                    ? resolution.profile().getTopP().doubleValue()
                    : null;
            Integer maxTokens = normalizeMaxTokens(resolution.profile().getMaxTokens());

            AiResponse result = aiCallExecutor.call(AiRequest.builder()
                    .provider(resolvedModel.provider())
                    .model(resolvedModel.model())
                    .messages(messages)
                    .temperature(temperature)
                    .topP(topP)
                    .maxTokens(maxTokens)
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

    private Integer normalizeMaxTokens(Integer maxTokens) {
        if (maxTokens == null || maxTokens <= 0) {
            return null;
        }
        return maxTokens;
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
