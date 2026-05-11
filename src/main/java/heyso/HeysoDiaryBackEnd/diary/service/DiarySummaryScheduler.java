package heyso.HeysoDiaryBackEnd.diary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DiarySummaryScheduler {
    private final DiarySummaryService diarySummaryService;

    public DiarySummaryScheduler(DiarySummaryService diarySummaryService) {
        this.diarySummaryService = diarySummaryService;
    }

    @Scheduled(cron = "${app.diary.summary.scheduler.cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void rebuildDirtySummaries() {
        List<Long> dirtyUserIds = diarySummaryService.getDirtyUserIds();
        if (dirtyUserIds.isEmpty()) {
            return;
        }

        log.info("Diary summary scheduler started. dirtyUserCount={}", dirtyUserIds.size());
        for (Long userId : dirtyUserIds) {
            try {
                diarySummaryService.rebuildSummary(userId);
            } catch (Exception e) {
                log.error("Diary summary rebuild failed. userId={}", userId, e);
            }
        }
        log.info("Diary summary scheduler finished. dirtyUserCount={}", dirtyUserIds.size());
    }
}
