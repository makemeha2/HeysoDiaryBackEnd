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
public class AiPromptBindingDetailResponse {

    private Long bindingId;
    private String bindingName;
    private String domainType;
    private String featureKey;
    private Long systemTemplateId;
    private String systemTemplateName;
    private Long userTemplateId;
    private String userTemplateName;
    private Long runtimeProfileId;
    private String profileName;
    private String description;
    private Integer isActive;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;
}
