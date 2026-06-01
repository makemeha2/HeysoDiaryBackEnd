package heyso.HeysoDiaryBackEnd.userMemorySnapshot.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshot;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotEventSource;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotProfileSource;

@Mapper
public interface UserMemorySnapshotMapper {
    List<Long> selectChangedUserIds();

    List<UserMemorySnapshotEventSource> selectActiveEventSources(@Param("userId") Long userId,
            @Param("sourceFromDate") LocalDate sourceFromDate,
            @Param("sourceToDate") LocalDate sourceToDate);

    List<UserMemorySnapshotProfileSource> selectActiveProfileSources(@Param("userId") Long userId);

    int selectNextSnapshotVersion(@Param("userId") Long userId);

    void deactivateActiveSnapshots(@Param("userId") Long userId);

    void insertSnapshot(UserMemorySnapshot snapshot);
}
