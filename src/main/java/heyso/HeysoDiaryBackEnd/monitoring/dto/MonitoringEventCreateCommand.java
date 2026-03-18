package heyso.HeysoDiaryBackEnd.monitoring.dto;

import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventType;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringSeverity;
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
public class MonitoringEventCreateCommand {
    private MonitoringEventType eventType;
    private String eventCode;
    private MonitoringSeverity severity;
    private String title;
    private String message;
    private String detailJson;
    private Long userId;
    private String userRole;
    private String sourceClass;
    private String sourceMethod;
}
