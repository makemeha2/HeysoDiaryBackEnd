package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchExecutionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBatchExecuteResponse {
    private Long executionId;
    private String batchKey;
    private AdminBatchExecutionStatus status;
}
