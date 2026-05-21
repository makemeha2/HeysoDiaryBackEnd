package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.service.AdminBatchService;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchKeys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAnalysisScheduler {
    private final AdminBatchService adminBatchService;

    @Scheduled(cron = "${app.diary.analysis.scheduler.cron:0 */10 * * * *}", zone = "Asia/Seoul")
    public void analyzeDirtyDiaries() {
        adminBatchService.executeAuto(AdminBatchKeys.DIARY_ANALYSIS);
    }
}
