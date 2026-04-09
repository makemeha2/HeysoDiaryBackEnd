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
public class AiPromptBindingListResponse {

    private Long bindingId;
    private String bindingName;
    private String domainType;
    private String featureKey;
    private Long systemTemplateId;
    private Long userTemplateId;
    private Long runtimeProfileId;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
