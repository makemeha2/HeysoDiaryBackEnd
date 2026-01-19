package heyso.HeysoDiaryBackEnd.aichat.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatConversationSummary {
    private Long conversationId;

    private String summary;
    private Integer summaryVersion;
    private Long lastMessageId;

    private LocalDateTime updatedAt;
}
