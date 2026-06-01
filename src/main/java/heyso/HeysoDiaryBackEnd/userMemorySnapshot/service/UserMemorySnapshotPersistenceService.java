package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.userMemorySnapshot.mapper.UserMemorySnapshotMapper;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshot;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMemorySnapshotPersistenceService {
    private final UserMemorySnapshotMapper userMemorySnapshotMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void replaceActiveSnapshot(UserMemorySnapshot snapshot) {
        snapshot.setSnapshotVersion(userMemorySnapshotMapper.selectNextSnapshotVersion(snapshot.getUserId()));
        userMemorySnapshotMapper.deactivateActiveSnapshots(snapshot.getUserId());
        userMemorySnapshotMapper.insertSnapshot(snapshot);
    }
}
