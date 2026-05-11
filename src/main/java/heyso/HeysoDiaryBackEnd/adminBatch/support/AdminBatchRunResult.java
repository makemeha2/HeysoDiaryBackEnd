package heyso.HeysoDiaryBackEnd.adminBatch.support;

public record AdminBatchRunResult(
        int successCount,
        int failureCount,
        String message) {
}
