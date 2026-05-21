package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryTraitEvidence {
    private Long analysisId;
    private Long diaryId;
    private Long userId;
    private String traitKey;
    private BigDecimal signalScore;
    private BigDecimal confidence;
    private String evidenceText;
    private String reasonJson;
}
