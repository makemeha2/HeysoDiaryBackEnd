package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPromptBindingUpdateRequest {

    @NotBlank
    private String bindingName;

    @NotBlank
    private String domainType;

    @NotNull
    private Long systemTemplateId;

    @NotNull
    private Long userTemplateId;

    @NotNull
    private Long runtimeProfileId;

    private String description;

    private Integer isActive;
}
