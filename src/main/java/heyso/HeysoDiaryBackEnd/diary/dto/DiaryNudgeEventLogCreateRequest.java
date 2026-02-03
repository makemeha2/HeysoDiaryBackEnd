package heyso.HeysoDiaryBackEnd.diary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryNudgeEventLogCreateRequest {

    @NotNull
    private LocalDate localDate;

    @NotBlank
    @Size(max = 20)
    private String eventType;

    @Size(max = 64)
    private String messageKey;

    @Size(max = 255)
    private String messageText;

    private LocalDateTime clientTime;

    private String metadataJson;
}
