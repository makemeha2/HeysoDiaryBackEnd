package heyso.HeysoDiaryBackEnd.aiTemplate.model;

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
public class AiPromptTemplateRel {

    private Long relId;
    private Long parentTemplateId;
    private Long childTemplateId;
    private String mergeType;
    private Integer sortSeq;
    private Integer isActive;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;
}
