package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

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
public class AiPromptTemplateRelResponse {

    private Long relId;
    private Long parentTemplateId;
    private Long childTemplateId;
    private String childTemplateKey;
    private String childTemplateName;
    private String mergeType;
    private Integer sortSeq;
    private Integer isActive;
}
