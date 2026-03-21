package heyso.HeysoDiaryBackEnd.diaryAiPolish.model;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiPolishLog {

    private Long id;
    private Long userId;
    private Long diaryId;
    private Integer requestTextLength;
    private String requestStatus;
    private DiaryAiPolishFailureCode failReasonCode;
    private String usedQuotaYn;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
