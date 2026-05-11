package heyso.HeysoDiaryBackEnd.diary.service;

import java.util.List;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("diarySummaryBatchRunner")
@RequiredArgsConstructor
@Slf4j
public class DiarySummaryBatchRunner implements AdminBatchRunner {
    private final DiarySummaryService diarySummaryService;

    @Override
    public AdminBatchRunResult run() {
        List<Long> dirtyUserIds = diarySummaryService.getDirtyUserIds();
        if (dirtyUserIds.isEmpty()) {
            return new AdminBatchRunResult(0, 0, "재집계 대상 사용자가 없습니다.");
        }

        int successCount = 0;
        int failureCount = 0;
        log.info("Diary summary batch started. dirtyUserCount={}", dirtyUserIds.size());
        for (Long userId : dirtyUserIds) {
            try {
                diarySummaryService.rebuildSummary(userId);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Diary summary rebuild failed. userId={}", userId, e);
            }
        }
        log.info("Diary summary batch finished. dirtyUserCount={}, successCount={}, failureCount={}",
                dirtyUserIds.size(), successCount, failureCount);

        return new AdminBatchRunResult(
                successCount,
                failureCount,
                "처리 대상 " + dirtyUserIds.size() + "건 중 성공 " + successCount + "건, 실패 " + failureCount + "건");
    }
}
