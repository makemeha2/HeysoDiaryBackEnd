package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPromptTemplateRelCreateRequest {

    @NotNull
    private Long childTemplateId;

    @NotBlank
    private String mergeType;

    private int sortSeq = 0;
}
