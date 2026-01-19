package heyso.HeysoDiaryBackEnd.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageCreateRequest {

    @NotBlank
    @Pattern(regexp = "SYSTEM|USER|ASSISTANT|TOOL", message = "role must be one of SYSTEM, USER, ASSISTANT, TOOL")
    private String role;

    @NotBlank
    private String content;

    @Size(max = 16)
    @Pattern(regexp = "text|markdown|json", message = "contentFormat must be one of text, markdown, json")
    private String contentFormat = "markdown";

    private Integer tokenCount;
    private Long parentMessageId;

    @Size(max = 64)
    private String clientMessageId;
}
