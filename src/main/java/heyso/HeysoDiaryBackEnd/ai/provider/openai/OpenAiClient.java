package heyso.HeysoDiaryBackEnd.ai.provider.openai;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiClient;
import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiTimed;
import org.springframework.ai.chat.client.ChatClient;

@Component
public class OpenAiClient implements AiClient {

    private final ChatClient chatClient;

    public OpenAiClient(@Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public AiProvider provider() {
        return AiProvider.OPENAI;
    }

    @Override
    @AiTimed(domain = "openai_client", phase = "ai_http_call")
    public AiResponse generate(AiRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI request is required");
        }

        List<Message> springMessages = toSpringMessages(request.messages());

        ChatOptions.Builder optionsBuilder = ChatOptions.builder().model(request.model());
        if (request.temperature() != null) {
            optionsBuilder.temperature(request.temperature());
        }
        if (request.topP() != null) {
            optionsBuilder.topP(request.topP());
        }
        if (request.maxTokens() != null) {
            optionsBuilder.maxTokens(request.maxTokens());
        }

        var responseSpec = chatClient.prompt()
                .messages(springMessages)
                .options(optionsBuilder.build())
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

        return new AiResponse(
                content,
                AiProvider.OPENAI,
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
}
