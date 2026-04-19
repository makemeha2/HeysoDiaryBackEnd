package heyso.HeysoDiaryBackEnd.monitoringMng.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonitoringEventDiagnoseResponse {
    private String diagnosis;
}
