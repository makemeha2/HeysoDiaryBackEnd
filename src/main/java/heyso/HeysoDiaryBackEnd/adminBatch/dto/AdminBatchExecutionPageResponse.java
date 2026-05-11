package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBatchExecutionPageResponse {
    private List<AdminBatchExecutionRow> items;
    private int page;
    private int size;
    private long totalCount;
    private int totalPages;
}
