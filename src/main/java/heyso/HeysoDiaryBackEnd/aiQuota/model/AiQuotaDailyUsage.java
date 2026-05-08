package heyso.HeysoDiaryBackEnd.aiQuota.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiQuotaDailyUsage {

    private Long id;
    private Long userId;
    private LocalDate usageDate;
    private Integer usedCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
