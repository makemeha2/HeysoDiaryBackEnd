package heyso.HeysoDiaryBackEnd.diaryAiPolish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishMode;

@Getter
@Setter
public class DiaryAiPolishRequest {

    private Long diaryId;

    @NotBlank(message = "content must not be blank")
    private String content;

    private DiaryAiPolishMode mode;
}
