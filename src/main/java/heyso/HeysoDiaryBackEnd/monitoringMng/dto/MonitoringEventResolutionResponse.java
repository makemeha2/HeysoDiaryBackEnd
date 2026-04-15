package heyso.HeysoDiaryBackEnd.monitoringMng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringEventResolutionResponse {
    private int requestedCount;
    private int successCount;
    private int skippedCount;
    private int failedCount;
}
