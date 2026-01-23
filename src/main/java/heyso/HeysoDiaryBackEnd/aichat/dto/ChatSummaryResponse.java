package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatSummaryResponse {
    private final Long conversationId;
    private final String summary;
    private final Integer summaryVersion;
    private final Long lastMessageId;
    private final LocalDateTime updatedAt;

    public ChatSummaryResponse(Long conversationId, String summary, Integer summaryVersion, Long lastMessageId,
            LocalDateTime updatedAt) {
        this.conversationId = conversationId;
        this.summary = summary;
        this.summaryVersion = summaryVersion;
        this.lastMessageId = lastMessageId;
        this.updatedAt = updatedAt;
    }
}
