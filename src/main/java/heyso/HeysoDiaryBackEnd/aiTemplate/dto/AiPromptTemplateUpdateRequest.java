package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPromptTemplateUpdateRequest {

    @NotBlank
    private String templateName;

    @NotBlank
    private String domainType;

    private String featureKey;

    @NotBlank
    private String templateRole;

    @NotBlank
    private String templateType;

    @NotBlank
    private String content;

    private String variablesSchemaJson;

    private String description;

    private Integer isActive;
}
