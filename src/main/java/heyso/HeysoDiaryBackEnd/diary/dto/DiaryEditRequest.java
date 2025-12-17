package heyso.HeysoDiaryBackEnd.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryEditRequest {

    @NotNull
    private Long diaryId;

    @NotBlank
    private String title;

    @NotBlank
    private String contentMd;

    @NotNull
    private LocalDate diaryDate;

    private List<String> tags;
}
