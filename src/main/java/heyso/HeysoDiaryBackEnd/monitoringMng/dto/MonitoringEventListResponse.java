package heyso.HeysoDiaryBackEnd.monitoringMng.dto;

import java.time.LocalDateTime;

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
public class MonitoringEventListResponse {
    private Long eventId;
    private LocalDateTime createdAt;
    private String eventType;
    private String severity;
    private String eventCode;
    private String title;
    private String requestUri;
    private String clientIp;
    private Long userId;
    private String traceId;
    private String sourceClass;
    private String resolvedYn;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
}
