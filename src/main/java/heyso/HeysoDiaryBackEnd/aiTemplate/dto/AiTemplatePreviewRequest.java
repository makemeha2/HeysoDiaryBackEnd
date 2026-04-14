package heyso.HeysoDiaryBackEnd.aiTemplate.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiTemplatePreviewRequest {

    private Map<String, String> variables;
}
