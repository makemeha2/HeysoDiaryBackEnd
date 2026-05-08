package heyso.HeysoDiaryBackEnd.aiQuota.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiQuotaGrant {

    private Long id;
    private Long userId;
    private LocalDate grantDate;
    private Integer amount;
    private String sourceType;
    private String sourceRef;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
