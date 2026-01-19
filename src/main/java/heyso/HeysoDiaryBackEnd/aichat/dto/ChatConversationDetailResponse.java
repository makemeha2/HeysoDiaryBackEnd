package heyso.HeysoDiaryBackEnd.aichat.dto;

import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ChatConversationDetailResponse {
    private final Long conversationId;
    private final String title;
    private final String model;
    private final String systemPrompt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ChatMessageResponse> messages;

    public ChatConversationDetailResponse(Long conversationId, String title, String model, String systemPrompt,
                                         LocalDateTime createdAt, LocalDateTime updatedAt, List<ChatMessageResponse> messages) {
        this.conversationId = conversationId;
        this.title = title;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    public static ChatConversationDetailResponse of(ChatConversation c, List<ChatMessageResponse> messages) {
        return new ChatConversationDetailResponse(
                c.getConversationId(),
                c.getTitle(),
                c.getModel(),
                c.getSystemPrompt(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                messages
        );
    }
}
