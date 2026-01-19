package heyso.HeysoDiaryBackEnd.aichat.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatConversationUpdateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 50)
    private String model;

    private String systemPrompt;
}
