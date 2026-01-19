package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatAssistantReplyResponse {
    private Long userMessageId;
    private Long assistantMessageId;

    private String model;
    private String requestId;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    private String assistantContent;
}
