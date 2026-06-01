package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.service.UserMemorySnapshotService;

@ExtendWith(MockitoExtension.class)
class UserTraitProfileBatchRunnerTest {
    @Mock
    private UserTraitProfileService userTraitProfileService;

    @Mock
    private UserMemorySnapshotService userMemorySnapshotService;

    @InjectMocks
    private UserTraitProfileBatchRunner runner;

    @Test
    @DisplayName("profile batch가 성공하면 memory snapshot batch를 이어 실행한다")
    void run_executesSnapshotAfterProfileSuccess() {
        when(userTraitProfileService.rebuildChangedUserProfiles())
                .thenReturn(new AdminBatchRunResult(2, 0, "profile ok"));
        when(userMemorySnapshotService.rebuildChangedUserSnapshots())
                .thenReturn(new AdminBatchRunResult(1, 0, "snapshot ok"));

        AdminBatchRunResult result = runner.run();

        verify(userMemorySnapshotService).rebuildChangedUserSnapshots();
        assertThat(result.successCount()).isEqualTo(3);
        assertThat(result.failureCount()).isZero();
        assertThat(result.message()).contains("profile ok", "snapshot ok");
    }

    @Test
    @DisplayName("profile batch에 실패가 있으면 memory snapshot 자동 실행을 건너뛴다")
    void run_skipsSnapshotWhenProfileHasFailure() {
        when(userTraitProfileService.rebuildChangedUserProfiles())
                .thenReturn(new AdminBatchRunResult(1, 1, "profile partial"));

        AdminBatchRunResult result = runner.run();

        verify(userMemorySnapshotService, never()).rebuildChangedUserSnapshots();
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.message()).contains("memory snapshot은 profile 실패가 있어 건너뜀");
    }
}
