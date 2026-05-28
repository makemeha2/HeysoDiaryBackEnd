package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;
import lombok.RequiredArgsConstructor;

@Component("userTraitProfileBatchRunner")
@RequiredArgsConstructor
public class UserTraitProfileBatchRunner implements AdminBatchRunner {
    private final UserTraitProfileService userTraitProfileService;

    @Override
    public AdminBatchRunResult run() {
        return userTraitProfileService.rebuildChangedUserProfiles();
    }
}
