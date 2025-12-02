package heyso.HeysoDiaryBackEnd.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryCreateRequest {

    @Positive
    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String contentMd;

    @NotNull
    private LocalDate diaryDate;
}
