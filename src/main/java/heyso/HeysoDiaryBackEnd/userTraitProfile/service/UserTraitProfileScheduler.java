package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.service.AdminBatchService;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchKeys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserTraitProfileScheduler {
    private final AdminBatchService adminBatchService;

    @Scheduled(cron = "${app.user-trait-profile.scheduler.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void rebuildUserTraitProfiles() {
        adminBatchService.executeAuto(AdminBatchKeys.USER_TRAIT_PROFILE);
    }
}
