package heyso.HeysoDiaryBackEnd.aichat.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private Long messageId;
    private Long conversationId;

    /** SYSTEM, USER, ASSISTANT, TOOL */
    private String role;

    private String content;
    /** text, markdown, json */
    private String contentFormat;

    private Integer tokenCount;
    private Long parentMessageId;
    private String clientMessageId;

    private LocalDateTime createdAt;
}
