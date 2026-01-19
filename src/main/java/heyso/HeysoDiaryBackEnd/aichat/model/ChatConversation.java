package heyso.HeysoDiaryBackEnd.aichat.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatConversation {
    private Long conversationId;
    private Long userId;

    private String title;
    private String model;
    private String systemPrompt;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
