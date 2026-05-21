package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDiaryAnalysisDiaryRow {
    private Long diaryId;
    private Long userId;
    private String authorNickname;
    private String title;
    private LocalDate diaryDate;
    private String moodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean dirty;
    private String analysisStatus;
    private String contentHash;
    private LocalDateTime contentUpdatedAt;
    private LocalDateTime lastMarkedAt;
    private LocalDateTime lastAnalyzedAt;
    private Long activeAnalysisId;
    private String lastErrorCode;
    private String lastErrorMessage;
    private LocalDateTime stateUpdatedAt;

    private Long latestAnalysisId;
    private Integer latestAnalysisVersion;
    private String latestAnalysisStatus;
    private Boolean latestAnalysisActive;
    private String latestSummaryText;
    private String latestErrorCode;
    private String latestErrorMessage;
    private LocalDateTime latestStartedAt;
    private LocalDateTime latestFinishedAt;

    private Integer eventCount;
    private Integer evidenceCount;
}
