package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import java.time.LocalDateTime;
import java.util.List;

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
public class AiPromptTemplateDetailResponse {

    private Long templateId;
    private String templateKey;
    private String templateName;
    private String domainType;
    private String featureKey;
    private String templateRole;
    private String templateType;
    private String content;
    private String variablesSchemaJson;
    private String description;
    private Integer revisionNo;
    private Integer isActive;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;

    private List<AiPromptTemplateRelResponse> relations;
}
