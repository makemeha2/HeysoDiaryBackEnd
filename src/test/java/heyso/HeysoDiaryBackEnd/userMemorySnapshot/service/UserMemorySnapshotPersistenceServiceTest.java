package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.userMemorySnapshot.mapper.UserMemorySnapshotMapper;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshot;

@ExtendWith(MockitoExtension.class)
class UserMemorySnapshotPersistenceServiceTest {
    @Mock
    private UserMemorySnapshotMapper userMemorySnapshotMapper;

    @InjectMocks
    private UserMemorySnapshotPersistenceService service;

    @Test
    @DisplayName("기존 active snapshot 비활성화 후 다음 version 신규 active snapshot을 저장한다")
    void replaceActiveSnapshot_deactivatesAndInsertsNextVersion() {
        UserMemorySnapshot snapshot = new UserMemorySnapshot();
        snapshot.setUserId(10L);
        snapshot.setSummaryText("요약");
        snapshot.setSourceFromDate(LocalDate.of(2026, 3, 4));
        snapshot.setSourceToDate(LocalDate.of(2026, 6, 1));
        when(userMemorySnapshotMapper.selectNextSnapshotVersion(10L)).thenReturn(3);

        service.replaceActiveSnapshot(snapshot);

        InOrder order = inOrder(userMemorySnapshotMapper);
        order.verify(userMemorySnapshotMapper).selectNextSnapshotVersion(10L);
        order.verify(userMemorySnapshotMapper).deactivateActiveSnapshots(10L);

        ArgumentCaptor<UserMemorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(UserMemorySnapshot.class);
        order.verify(userMemorySnapshotMapper).insertSnapshot(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getValue().getSnapshotVersion()).isEqualTo(3);
    }
}
