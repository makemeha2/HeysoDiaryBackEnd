package heyso.HeysoDiaryBackEnd.diaryAi.model;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiFeedbackType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAiFeedback {

    private Long feedbackId;

    private Long aiCommentId;
    private Long userId;

    private DiaryAiFeedbackType feedbackType;
    private String feedbackReason;

    private LocalDateTime createdAt;
}
