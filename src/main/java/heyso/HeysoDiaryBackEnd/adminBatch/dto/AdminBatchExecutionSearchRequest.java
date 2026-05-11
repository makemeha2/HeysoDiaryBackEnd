package heyso.HeysoDiaryBackEnd.adminBatch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminBatchExecutionSearchRequest {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 20;

    public int getOffset() {
        return (page - 1) * size;
    }
}
