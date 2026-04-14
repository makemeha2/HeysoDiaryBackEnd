package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRuntimeProfileCreateRequest {

    @NotBlank
    private String profileKey;

    @NotBlank
    private String profileName;

    @NotBlank
    private String domainType;

    private String provider;

    @NotBlank
    private String model;

    private BigDecimal temperature;

    private BigDecimal topP;

    private Integer maxTokens;

    private String description;
}
