package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

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
public class AiRuntimeProfileListResponse {

    private Long runtimeProfileId;
    private String profileKey;
    private String profileName;
    private String domainType;
    private String provider;
    private String model;
    private String modelName;
    private BigDecimal temperature;
    private BigDecimal topP;
    private Integer maxTokens;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
