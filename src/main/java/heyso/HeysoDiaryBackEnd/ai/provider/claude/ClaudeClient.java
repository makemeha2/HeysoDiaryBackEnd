package heyso.HeysoDiaryBackEnd.ai.provider.claude;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiClient;
import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClaudeClient implements AiClient {

    private final ChatClient chatClient;

    public ClaudeClient(@Qualifier("claudeChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public AiProvider provider() {
        return AiProvider.CLAUDE;
    }

    @Override
    public AiResponse generate(AiRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI request is required");
        }

        String key = System.getenv("CLAUDE_API_KEY");
        log.info("Claude API key present={}, length={}", key != null, key == null ? 0 : key.length());

        List<Message> springMessages = toSpringMessages(request.messages());

        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
                .model(request.model());
        if (request.temperature() != null) {
            optionsBuilder.temperature(request.temperature());
        }
        if (request.topP() != null) {
            optionsBuilder.topP(request.topP());
        }
        if (request.maxTokens() != null) {
            optionsBuilder.maxTokens(request.maxTokens());
        }
        AnthropicChatOptions options = optionsBuilder.build();

        log.info("Claude API request payload: {}", buildRequestLog(request));

        var responseSpec = chatClient.prompt()
                .messages(springMessages)
                .options(options)
                .call();

        var chatResponse = responseSpec.chatResponse();
        String content = responseSpec.content();

        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI content is empty");
        }

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
            // usage 파싱 실패 시 본문 우선 반환
        }

        log.info(
                "Claude API response summary: requestId={}, model={}, promptTokens={}, completionTokens={}, totalTokens={}, contentLength={}",
                requestId, request.model(), promptTokens, completionTokens, totalTokens, content.length());

        return new AiResponse(
                content,
                AiProvider.CLAUDE,
                request.model(),
                requestId,
                promptTokens,
                completionTokens,
                totalTokens);
    }

    private List<Message> toSpringMessages(List<AiMessage> messages) {
        List<Message> springMessages = new ArrayList<>();
        if (messages == null) {
            return springMessages;
        }

        for (AiMessage message : messages) {
            if (message == null || message.role() == null) {
                continue;
            }

            switch (message.role()) {
                case "developer", "system" -> springMessages.add(new SystemMessage(message.content()));
                case "user" -> springMessages.add(new UserMessage(message.content()));
                case "assistant" -> springMessages.add(new AssistantMessage(message.content()));
                default -> {
                    // tool 등은 미지원
                }
            }
        }
        return springMessages;
    }

    private String buildRequestLog(AiRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("{provider=").append(request.provider());
        builder.append(", model=").append(request.model());
        builder.append(", temperature=").append(request.temperature());
        builder.append(", topP=").append(request.topP());
        builder.append(", maxTokens=").append(request.maxTokens());
        builder.append(", messages=").append(formatMessages(request.messages()));
        builder.append('}');
        return builder.toString();
    }

    private String formatMessages(List<AiMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            AiMessage message = messages.get(i);
            if (i > 0) {
                builder.append(", ");
            }

            if (message == null) {
                builder.append("{role=null, content=null}");
                continue;
            }

            builder.append("{role=").append(message.role())
                    .append(", content=").append(quote(message.content()))
                    .append("}");
        }
        builder.append(']');
        return builder.toString();
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }

        return '"'
                + value.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n")
                + '"';
    }
}
