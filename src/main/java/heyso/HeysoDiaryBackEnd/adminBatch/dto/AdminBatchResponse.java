package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBatchResponse {
    private String batchKey;
    private String name;
    private String description;
    private String cronExpression;
    private String zone;
    private boolean running;
    private AdminBatchExecutionRow latestExecution;
}
