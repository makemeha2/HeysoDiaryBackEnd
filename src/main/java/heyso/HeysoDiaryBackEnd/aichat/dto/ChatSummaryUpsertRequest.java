package heyso.HeysoDiaryBackEnd.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSummaryUpsertRequest {

    @NotBlank
    private String summary;

    @Positive
    private Integer summaryVersion = 1;

    private Long lastMessageId;
}
