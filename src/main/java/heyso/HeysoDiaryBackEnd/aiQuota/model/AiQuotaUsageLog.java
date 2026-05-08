package heyso.HeysoDiaryBackEnd.aiQuota.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiQuotaUsageLog {

    private Long id;
    private Long userId;
    private LocalDate usageDate;
    private AiFeatureType featureType;
    private Long featureRefId;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
