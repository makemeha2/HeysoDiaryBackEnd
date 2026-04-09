package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

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
public class AiPromptTemplateListResponse {

    private Long templateId;
    private String templateKey;
    private String templateName;
    private String domainType;
    private String featureKey;
    private String templateRole;
    private String templateType;
    private Integer revisionNo;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
