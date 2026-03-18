package heyso.HeysoDiaryBackEnd.monitoring.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringEvent {
    private Long eventId;
    private String eventType;
    private String eventCode;
    private String severity;
    private String title;
    private String message;
    private String detailJson;
    private String httpMethod;
    private String requestUri;
    private String queryString;
    private String clientIp;
    private String userAgent;
    private Long userId;
    private String userRole;
    private String traceId;
    private String sessionId;
    private String exceptionClass;
    private String exceptionMessage;
    private String stackTrace;
    private String sourceClass;
    private String sourceMethod;
    private String resolvedYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
