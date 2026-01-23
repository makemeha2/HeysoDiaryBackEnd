package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {
    private final Long messageId;
    private final Long conversationId;
    private final String role;
    private final String content;
    private final String contentFormat;
    private final Integer tokenCount;
    private final Long parentMessageId;
    private final String clientMessageId;
    private final LocalDateTime createdAt;

    public ChatMessageResponse(Long messageId, Long conversationId, String role, String content, String contentFormat,
            Integer tokenCount, Long parentMessageId, String clientMessageId, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.contentFormat = contentFormat;
        this.tokenCount = tokenCount;
        this.parentMessageId = parentMessageId;
        this.clientMessageId = clientMessageId;
        this.createdAt = createdAt;
    }
}
