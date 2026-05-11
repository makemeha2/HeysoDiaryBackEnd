package heyso.HeysoDiaryBackEnd.adminBatch.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import heyso.HeysoDiaryBackEnd.adminBatch.mapper.AdminBatchExecutionMapper;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchDefinition;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBatchAsyncExecutor {
    private static final int MESSAGE_MAX = 500;
    private static final int ERROR_MESSAGE_MAX = 1000;

    private final AdminBatchRegistry adminBatchRegistry;
    private final AdminBatchExecutionMapper adminBatchExecutionMapper;

    @Async("adminBatchExecutor")
    public void runExecution(Long executionId, String batchKey) {
        long started = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        String message = null;

        try {
            AdminBatchDefinition definition = adminBatchRegistry.getDefinition(batchKey);
            if (definition == null || definition.runner() == null) {
                throw new IllegalStateException("Batch definition not found: " + batchKey);
            }

            AdminBatchRunResult result = definition.runner().run();
            successCount = result.successCount();
            failureCount = result.failureCount();
            message = truncate(result.message(), MESSAGE_MAX);
            long durationMs = System.currentTimeMillis() - started;

            if (failureCount > 0) {
                adminBatchExecutionMapper.updateExecutionFailed(
                        executionId,
                        durationMs,
                        successCount,
                        failureCount,
                        message,
                        "일부 항목 처리에 실패했습니다. 서버 로그를 확인하세요.");
                return;
            }

            adminBatchExecutionMapper.updateExecutionSuccess(
                    executionId,
                    durationMs,
                    successCount,
                    failureCount,
                    message);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - started;
            String errorMessage = truncate(e.getClass().getSimpleName() + ": " + e.getMessage(), ERROR_MESSAGE_MAX);
            log.error("Admin batch execution failed. executionId={}, batchKey={}", executionId, batchKey, e);
            adminBatchExecutionMapper.updateExecutionFailed(
                    executionId,
                    durationMs,
                    successCount,
                    failureCount,
                    message,
                    errorMessage);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
