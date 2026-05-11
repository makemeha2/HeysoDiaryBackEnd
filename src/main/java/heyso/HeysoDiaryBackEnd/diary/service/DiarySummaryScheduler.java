package heyso.HeysoDiaryBackEnd.diary.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.service.AdminBatchService;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchKeys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiarySummaryScheduler {
    private final AdminBatchService adminBatchService;

    @Scheduled(cron = "${app.diary.summary.scheduler.cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void rebuildDirtySummaries() {
        adminBatchService.executeAuto(AdminBatchKeys.DIARY_SUMMARY_REBUILD);
    }
}
