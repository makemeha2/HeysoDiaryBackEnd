package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDiaryTraitEvidenceRow {
    private Long evidenceId;
    private Long analysisId;
    private Long diaryId;
    private Long userId;
    private String traitKey;
    private String traitName;
    private BigDecimal signalScore;
    private BigDecimal confidence;
    private String evidenceText;
    private String reasonJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
