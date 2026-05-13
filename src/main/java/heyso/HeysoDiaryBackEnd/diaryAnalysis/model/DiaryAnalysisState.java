package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAnalysisState {
    private Long diaryId;
    private Long userId;
    private Boolean dirty;
    private String analysisStatus;
    private String contentHash;
    private LocalDateTime contentUpdatedAt;
    private LocalDateTime lastMarkedAt;
    private LocalDateTime lastAnalyzedAt;
    private Long activeAnalysisId;
    private String lastErrorCode;
    private String lastErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
