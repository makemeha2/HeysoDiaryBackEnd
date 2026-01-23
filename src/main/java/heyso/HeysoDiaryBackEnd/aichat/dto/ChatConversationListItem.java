package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatConversationListItem {
    private final Long conversationId;
    private final String title;
    private final String model;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ChatConversationListItem(Long conversationId, String title, String model, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.conversationId = conversationId;
        this.title = title;
        this.model = model;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
