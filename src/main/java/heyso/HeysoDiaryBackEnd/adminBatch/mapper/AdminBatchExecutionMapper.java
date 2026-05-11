package heyso.HeysoDiaryBackEnd.adminBatch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionRow;
import heyso.HeysoDiaryBackEnd.adminBatch.dto.AdminBatchExecutionSearchRequest;
import heyso.HeysoDiaryBackEnd.adminBatch.model.AdminBatchExecution;

@Mapper
public interface AdminBatchExecutionMapper {
    int insertRunningExecution(AdminBatchExecution execution);

    AdminBatchExecutionRow selectLatestExecution(@Param("batchKey") String batchKey);

    List<AdminBatchExecutionRow> selectExecutionPage(
            @Param("batchKey") String batchKey,
            @Param("request") AdminBatchExecutionSearchRequest request);

    long countExecutions(@Param("batchKey") String batchKey);

    int updateExecutionSuccess(
            @Param("executionId") Long executionId,
            @Param("durationMs") long durationMs,
            @Param("successCount") int successCount,
            @Param("failureCount") int failureCount,
            @Param("message") String message);

    int updateExecutionFailed(
            @Param("executionId") Long executionId,
            @Param("durationMs") long durationMs,
            @Param("successCount") int successCount,
            @Param("failureCount") int failureCount,
            @Param("message") String message,
            @Param("errorMessage") String errorMessage);
}
