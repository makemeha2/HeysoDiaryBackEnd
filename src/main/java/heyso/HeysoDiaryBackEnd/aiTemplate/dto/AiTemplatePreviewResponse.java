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
public class AiTemplatePreviewResponse {

    private Long templateId;
    private String templateKey;
    private String renderedContent;
}
