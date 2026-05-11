package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBatchListResponse {
    private List<AdminBatchResponse> items;
}
