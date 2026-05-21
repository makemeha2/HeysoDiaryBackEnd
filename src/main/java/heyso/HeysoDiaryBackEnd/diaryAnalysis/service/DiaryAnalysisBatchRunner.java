package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;
import lombok.RequiredArgsConstructor;

@Component("diaryAnalysisBatchRunner")
@RequiredArgsConstructor
public class DiaryAnalysisBatchRunner implements AdminBatchRunner {
    private final DiaryAnalysisService diaryAnalysisService;

    @Override
    public AdminBatchRunResult run() {
        return diaryAnalysisService.analyzePendingDiaries();
    }
}
