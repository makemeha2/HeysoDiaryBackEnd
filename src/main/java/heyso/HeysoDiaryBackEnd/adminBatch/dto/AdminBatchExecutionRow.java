package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchExecutionStatus;
import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchTriggerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminBatchExecutionRow {
    private Long executionId;
    private String batchKey;
    private AdminBatchTriggerType triggerType;
    private AdminBatchExecutionStatus status;
    private Long requestedBy;
    private String requestedByEmail;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private Integer successCount;
    private Integer failureCount;
    private String message;
    private String errorMessage;
}
