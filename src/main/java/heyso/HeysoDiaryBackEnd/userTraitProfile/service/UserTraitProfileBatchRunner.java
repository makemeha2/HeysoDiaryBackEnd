package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.service.UserMemorySnapshotService;
import lombok.RequiredArgsConstructor;

@Component("userTraitProfileBatchRunner")
@RequiredArgsConstructor
public class UserTraitProfileBatchRunner implements AdminBatchRunner {
    private final UserTraitProfileService userTraitProfileService;
    private final UserMemorySnapshotService userMemorySnapshotService;

    @Override
    public AdminBatchRunResult run() {
        AdminBatchRunResult profileResult = userTraitProfileService.rebuildChangedUserProfiles();
        if (profileResult.failureCount() > 0) {
            return new AdminBatchRunResult(
                    profileResult.successCount(),
                    profileResult.failureCount(),
                    profileResult.message() + " / memory snapshot은 profile 실패가 있어 건너뜀");
        }

        AdminBatchRunResult snapshotResult = userMemorySnapshotService.rebuildChangedUserSnapshots();
        return new AdminBatchRunResult(
                profileResult.successCount() + snapshotResult.successCount(),
                snapshotResult.failureCount(),
                profileResult.message() + " / " + snapshotResult.message());
    }
}
