package heyso.HeysoDiaryBackEnd.aichat.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatConversationCreateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 50)
    private String model = "gpt-4o-mini";

    private String systemPrompt;
}
