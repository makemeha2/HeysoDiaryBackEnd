package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.util.List;

public record DiaryAnalysisResult(
        String summary,
        List<DiaryEvent> events,
        List<DiaryTraitEvidence> traitEvidence,
        String rawResponseJson) {
}
