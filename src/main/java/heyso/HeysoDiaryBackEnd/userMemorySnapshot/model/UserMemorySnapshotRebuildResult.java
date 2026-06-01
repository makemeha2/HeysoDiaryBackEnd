package heyso.HeysoDiaryBackEnd.userMemorySnapshot.model;

public record UserMemorySnapshotRebuildResult(
        int createdSnapshotCount,
        int skippedCount) {
}
