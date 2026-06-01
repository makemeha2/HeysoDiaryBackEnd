package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;
import lombok.RequiredArgsConstructor;

@Component("userMemorySnapshotBatchRunner")
@RequiredArgsConstructor
public class UserMemorySnapshotBatchRunner implements AdminBatchRunner {
    private final UserMemorySnapshotService userMemorySnapshotService;

    @Override
    public AdminBatchRunResult run() {
        return userMemorySnapshotService.rebuildChangedUserSnapshots();
    }
}
