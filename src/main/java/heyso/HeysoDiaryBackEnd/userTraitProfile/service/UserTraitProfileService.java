package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.userTraitProfile.mapper.UserTraitProfileMapper;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfileRebuildResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTraitProfileService {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final int RECENT_WINDOW_DAYS = 30;

    private final UserTraitProfileMapper userTraitProfileMapper;
    private final UserTraitProfilePersistenceService persistenceService;

    @Transactional(readOnly = true)
    public List<Long> getChangedUserIds() {
        return userTraitProfileMapper.selectChangedUserIds();
    }

    public AdminBatchRunResult rebuildChangedUserProfiles() {
        List<Long> userIds = getChangedUserIds();
        if (userIds.isEmpty()) {
            return new AdminBatchRunResult(0, 0, "trait profile 집계 대상 사용자가 없습니다.");
        }

        LocalDate calculatedDate = LocalDate.now(SEOUL_ZONE);
        LocalDate recentFromDate = calculatedDate.minusDays(RECENT_WINDOW_DAYS - 1L);
        int successCount = 0;
        int failureCount = 0;
        int upsertedProfileCount = 0;
        int deactivatedProfileCount = 0;

        log.info("User trait profile batch started. userCount={}", userIds.size());
        for (Long userId : userIds) {
            try {
                UserTraitProfileRebuildResult result = persistenceService.rebuildUserProfile(
                        userId,
                        calculatedDate,
                        recentFromDate);
                successCount++;
                upsertedProfileCount += result.upsertedProfileCount();
                deactivatedProfileCount += result.deactivatedProfileCount();
            } catch (Exception e) {
                failureCount++;
                log.error("User trait profile rebuild failed. userId={}", userId, e);
            }
        }
        log.info("User trait profile batch finished. userCount={}, successCount={}, failureCount={}, "
                + "upsertedProfileCount={}, deactivatedProfileCount={}",
                userIds.size(), successCount, failureCount, upsertedProfileCount, deactivatedProfileCount);

        return new AdminBatchRunResult(
                successCount,
                failureCount,
                "처리 대상 " + userIds.size()
                        + "명 중 성공 " + successCount
                        + "명, 실패 " + failureCount
                        + "명, 갱신 profile " + upsertedProfileCount
                        + "건, 비활성화 profile " + deactivatedProfileCount + "건");
    }
}
