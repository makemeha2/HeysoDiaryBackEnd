package heyso.HeysoDiaryBackEnd.adminBatch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecuteResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionPageResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionSearchRequest;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchListResponse;
import heyso.HeysoDiaryBackEnd.adminBatch.service.AdminBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/batches")
@RequiredArgsConstructor
public class AdminBatchController {
    private final AdminBatchService adminBatchService;

    @GetMapping
    public ResponseEntity<AdminBatchListResponse> getBatches() {
        return ResponseEntity.ok(adminBatchService.getBatches());
    }

    @GetMapping("/{batchKey}/executions")
    public ResponseEntity<AdminBatchExecutionPageResponse> getExecutionPage(
            @PathVariable String batchKey,
            @Valid AdminBatchExecutionSearchRequest request) {
        return ResponseEntity.ok(adminBatchService.getExecutionPage(batchKey, request));
    }

    @PostMapping("/{batchKey}/execute")
    public ResponseEntity<AdminBatchExecuteResponse> executeBatch(@PathVariable String batchKey) {
        return ResponseEntity.ok(adminBatchService.executeManual(batchKey));
    }
}
