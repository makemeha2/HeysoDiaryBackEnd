package heyso.HeysoDiaryBackEnd.monitoring.support;

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
public class MonitoringRequestContext {
    private String httpMethod;
    private String requestUri;
    private String queryString;
    private String clientIp;
    private String userAgent;
    private String sessionId;
    private String traceId;
}
