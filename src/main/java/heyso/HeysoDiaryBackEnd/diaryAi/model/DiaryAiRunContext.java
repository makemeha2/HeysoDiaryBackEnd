package heyso.HeysoDiaryBackEnd.diaryAi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiSourceType;
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
public class DiaryAiRunContext {

    private Long runContextId;
    private Long runId;

    private Long sourceDiaryId;
    private DiaryAiSourceType sourceType;

    private Integer sortOrder;
    private BigDecimal score;

    private LocalDateTime createdAt;
}
