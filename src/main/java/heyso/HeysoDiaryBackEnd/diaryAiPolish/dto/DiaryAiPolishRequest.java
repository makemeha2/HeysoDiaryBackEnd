package heyso.HeysoDiaryBackEnd.diaryAiPolish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiPolishRequest {

    private Long diaryId;

    @NotBlank(message = "content must not be blank")
    private String content;
}
