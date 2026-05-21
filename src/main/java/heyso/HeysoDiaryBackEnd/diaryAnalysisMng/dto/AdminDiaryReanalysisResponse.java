package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDiaryReanalysisResponse {
    private Long diaryId;
    private String analysisStatus;
    private boolean dirty;
    private String message;
}
