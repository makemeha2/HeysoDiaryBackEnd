package heyso.HeysoDiaryBackEnd.aichat.model;

import java.math.BigDecimal;
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
public class ChatUsageLog {
    private Long usageId;

    private Long userId;
    private Long conversationId;

    private String requestId;
    private String model;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    private BigDecimal costUsd;

    private LocalDateTime createdAt;
}
