package heyso.HeysoDiaryBackEnd.aiTemplate.model;

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
public class AiRuntimeProfile {

    private Long runtimeProfileId;
    private String profileKey;
    private String profileName;
    private String domainType;
    private String provider;
    private String model;
    private BigDecimal temperature;
    private BigDecimal topP;
    private Integer maxTokens;
    private String description;
    private Integer revisionNo;
    private Integer isActive;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;
}
