package heyso.HeysoDiaryBackEnd.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthError;
import heyso.HeysoDiaryBackEnd.monitoring.dto.MonitoringEventCreateCommand;
import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventType;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringSeverity;
import heyso.HeysoDiaryBackEnd.utils.MonitoringSecurityJsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MonitoringEventService monitoringEventService;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        JwtAuthError jwtAuthError = resolveJwtAuthError(request);

        if (!shouldSkipMonitoringLog(request)) {
            try {
                monitoringEventService.logEvent(
                        MonitoringEventCreateCommand.builder()
                                .eventType(MonitoringEventType.SECURITY)
                                .eventCode(MonitoringEventCode.SEC_INVALID_TOKEN.name())
                                .severity(MonitoringSeverity.HIGH)
                                .title("Authentication failure")
                                .message("Authentication failed while accessing protected resource")
                                .detailJson(MonitoringSecurityJsonUtil.buildDetailJson(
                                        request,
                                        "reason",
                                        jwtAuthError != null ? jwtAuthError.headerValue() : "authentication_failed"))
                                .sourceClass(getClass().getSimpleName())
                                .sourceMethod("commence")
                                .build(),
                        request);
            } catch (Exception e) {
                log.error("Failed to write authentication monitoring event", e);
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (jwtAuthError != null) {
            response.setHeader(JwtAuthError.RESPONSE_HEADER, jwtAuthError.headerValue());
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"message\":\"Unauthorized\"}");
    }

    private JwtAuthError resolveJwtAuthError(HttpServletRequest request) {
        Object value = request != null ? request.getAttribute(JwtAuthError.REQUEST_ATTRIBUTE) : null;
        return value instanceof JwtAuthError ? (JwtAuthError) value : null;
    }

    private boolean shouldSkipMonitoringLog(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String uri = request.getRequestURI();
        return uri != null && ("/actuator/health".equals(uri) || uri.startsWith("/actuator/health/"));
    }
}
