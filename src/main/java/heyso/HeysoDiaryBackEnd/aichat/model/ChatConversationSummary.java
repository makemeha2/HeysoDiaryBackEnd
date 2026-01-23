package heyso.HeysoDiaryBackEnd.aichat.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationSummary {
    private Long conversationId;

    private String summary;
    private Integer summaryVersion;
    private Long lastMessageId;

    private LocalDateTime updatedAt;
}
