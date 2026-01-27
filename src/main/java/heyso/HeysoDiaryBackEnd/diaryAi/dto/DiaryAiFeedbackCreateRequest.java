package heyso.HeysoDiaryBackEnd.diaryAi.dto;

import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiFeedbackType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiFeedbackCreateRequest {

    @NotNull
    private Long aiCommentId;

    @NotNull
    private DiaryAiFeedbackType feedbackType;

    @Size(max = 255)
    private String feedbackReason;
}
