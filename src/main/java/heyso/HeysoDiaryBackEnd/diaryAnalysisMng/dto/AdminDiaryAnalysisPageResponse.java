package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDiaryAnalysisPageResponse {
    private List<AdminDiaryAnalysisDiaryRow> items;
    private int page;
    private int size;
    private long totalCount;
    private int totalPages;
}
