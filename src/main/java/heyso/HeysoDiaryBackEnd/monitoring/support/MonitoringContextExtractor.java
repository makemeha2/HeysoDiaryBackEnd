package heyso.HeysoDiaryBackEnd.monitoring.support;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class MonitoringContextExtractor {

    public MonitoringRequestContext extract(HttpServletRequest request) {
        if (request == null) {
            return MonitoringRequestContext.builder().traceId(getTraceId()).build();
        }

        HttpSession session = request.getSession(false);
        return MonitoringRequestContext.builder()
                .httpMethod(request.getMethod())
                .requestUri(request.getRequestURI())
                .queryString(request.getQueryString())
                .clientIp(extractClientIp(request))
                .userAgent(request.getHeader(HttpHeaders.USER_AGENT))
                .sessionId(session != null ? session.getId() : null)
                .traceId(getTraceId())
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] ips = forwardedFor.split(",");
            return ips[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            return MDC.get("trace_id");
        }
        return traceId;
    }
}
