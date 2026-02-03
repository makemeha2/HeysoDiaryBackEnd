package heyso.HeysoDiaryBackEnd.aichat.openai;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;

@Component
public class AiCallExecutor {

    private final OpenAiClient openAiClient;

    public AiCallExecutor(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public AiCallResult call(String model, List<RoleMessage> messages, AiCallOptions options) {
        CallResponseSpec spec = openAiClient.createResponseSpec(
                model,
                messages,
                options == null ? null : options.temperature().orElse(null),
                options == null ? null : options.topP().orElse(null),
                options == null ? null : options.maxTokens().orElse(null));

        var chatResponse = spec.chatResponse();
        String content = spec.content();

        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI content is empty");
        }

        // requestId / usage 파싱 공통화
        String requestId = null;
        Integer promptTokens = null;
        Integer completionTokens = null;
        Integer totalTokens = null;

        try {
            if (chatResponse != null && chatResponse.getMetadata() != null) {
                requestId = chatResponse.getMetadata().getId();
            }

            Usage usage = (chatResponse == null) ? null : chatResponse.getMetadata().getUsage();
            if (usage != null && !(usage instanceof EmptyUsage)) {
                promptTokens = usage.getPromptTokens();
                completionTokens = usage.getCompletionTokens();
                totalTokens = usage.getTotalTokens();
            }
        } catch (Exception ignore) {
            // 메타데이터 파싱 실패는 "응답 본문(content)" 제공이 우선이므로 무시
        }

        return new AiCallResult(content, requestId, promptTokens, completionTokens, totalTokens);
    }
}
