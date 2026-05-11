package heyso.HeysoDiaryBackEnd.adminBatch.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecuteResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionPageResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionRow;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionSearchRequest;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchListResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.mapper.AdminBatchExecutionMapper;
import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchExecution;
import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchExecutionStatus;
import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchTriggerType;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchDefinition;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBatchService {
    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminBatchRegistry adminBatchRegistry;
    private final AdminBatchExecutionMapper adminBatchExecutionMapper;
    private final AdminBatchAsyncExecutor adminBatchAsyncExecutor;

    @Transactional(readOnly = true)
    public AdminBatchListResponse getBatches() {
        adminAuthorizationService.requireAdminUser();
        List<AdminBatchResponse> items = adminBatchRegistry.getDefinitions().stream()
                .map(this::toBatchResponse)
                .toList();
        return AdminBatchListResponse.builder()
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminBatchExecutionPageResponse getExecutionPage(
            String batchKey,
            AdminBatchExecutionSearchRequest request) {
        adminAuthorizationService.requireAdminUser();
        requireDefinition(batchKey);

        List<AdminBatchExecutionRow> items = adminBatchExecutionMapper.selectExecutionPage(batchKey, request);
        long totalCount = adminBatchExecutionMapper.countExecutions(batchKey);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / request.getSize());

        return AdminBatchExecutionPageResponse.builder()
                .items(items)
                .page(request.getPage())
                .size(request.getSize())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }

    public AdminBatchExecuteResponse executeManual(String batchKey) {
        User adminUser = adminAuthorizationService.requireAdminUser();
        AdminBatchExecution execution = createRunningExecution(batchKey, AdminBatchTriggerType.MANUAL,
                adminUser.getUserId());
        adminBatchAsyncExecutor.runExecution(execution.getExecutionId(), batchKey);
        return AdminBatchExecuteResponse.builder()
                .executionId(execution.getExecutionId())
                .batchKey(batchKey)
                .status(AdminBatchExecutionStatus.RUNNING)
                .build();
    }

    public void executeAuto(String batchKey) {
        try {
            AdminBatchExecution execution = createRunningExecution(batchKey, AdminBatchTriggerType.AUTO, null);
            adminBatchAsyncExecutor.runExecution(execution.getExecutionId(), batchKey);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
                log.info("Admin batch skipped because it is already running. batchKey={}", batchKey);
                return;
            }
            throw e;
        }
    }

    @Transactional
    public AdminBatchExecution createRunningExecution(
            String batchKey,
            AdminBatchTriggerType triggerType,
            Long requestedBy) {
        requireDefinition(batchKey);

        AdminBatchExecution execution = new AdminBatchExecution();
        execution.setBatchKey(batchKey);
        execution.setTriggerType(triggerType);
        execution.setStatus(AdminBatchExecutionStatus.RUNNING);
        execution.setRequestedBy(requestedBy);
        execution.setMessage("배치 실행이 시작되었습니다.");

        try {
            adminBatchExecutionMapper.insertRunningExecution(execution);
            return execution;
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Batch is already running");
        }
    }

    private AdminBatchResponse toBatchResponse(AdminBatchDefinition definition) {
        AdminBatchExecutionRow latestExecution = adminBatchExecutionMapper.selectLatestExecution(definition.batchKey());
        return AdminBatchResponse.builder()
                .batchKey(definition.batchKey())
                .name(definition.name())
                .description(definition.description())
                .cronExpression(definition.cronExpression())
                .zone(definition.zone())
                .running(latestExecution != null
                        && AdminBatchExecutionStatus.RUNNING.equals(latestExecution.getStatus()))
                .latestExecution(latestExecution)
                .build();
    }

    private AdminBatchDefinition requireDefinition(String batchKey) {
        AdminBatchDefinition definition = adminBatchRegistry.getDefinition(batchKey);
        if (definition == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found");
        }
        if (definition.runner() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Batch runner not configured");
        }
        return definition;
    }
}
