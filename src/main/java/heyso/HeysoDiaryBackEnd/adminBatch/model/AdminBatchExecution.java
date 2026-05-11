package heyso.HeysoDiaryBackEnd.adminBatch.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminBatchExecution {
    private Long executionId;
    private String batchKey;
    private AdminBatchTriggerType triggerType;
    private AdminBatchExecutionStatus status;
    private Long requestedBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private Integer successCount;
    private Integer failureCount;
    private String message;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
