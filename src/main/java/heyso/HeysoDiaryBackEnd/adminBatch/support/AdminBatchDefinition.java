package heyso.HeysoDiaryBackEnd.adminBatch.support;

public record AdminBatchDefinition(
        String batchKey,
        String name,
        String description,
        String cronExpression,
        String zone,
        AdminBatchRunner runner) {
}
