package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDiaryAnalysisSearchRequest {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 50;

    private Long userId;
    private Long diaryId;
    private String analysisStatus;
    private Boolean dirty;

    public int getOffset() {
        return (page - 1) * size;
    }
}
