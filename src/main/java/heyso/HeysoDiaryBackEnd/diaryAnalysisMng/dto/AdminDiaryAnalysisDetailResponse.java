package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDiaryAnalysisDetailResponse {
    private AdminDiaryAnalysisDiaryRow diary;
    private List<AdminDiaryEventRow> events;
    private List<AdminDiaryTraitEvidenceRow> traitEvidence;
}
