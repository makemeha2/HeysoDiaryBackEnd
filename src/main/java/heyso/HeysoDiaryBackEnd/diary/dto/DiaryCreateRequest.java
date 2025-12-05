package heyso.HeysoDiaryBackEnd.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DiaryCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String contentMd;

    @NotNull
    private LocalDate diaryDate;

    private List<String> tags;
}
