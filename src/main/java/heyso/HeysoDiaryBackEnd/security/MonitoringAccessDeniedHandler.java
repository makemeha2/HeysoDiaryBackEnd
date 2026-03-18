package heyso.HeysoDiaryBackEnd.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

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
public class MonitoringAccessDeniedHandler implements AccessDeniedHandler {

    private final MonitoringEventService monitoringEventService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        try {
            monitoringEventService.logEvent(
                    MonitoringEventCreateCommand.builder()
                            .eventType(MonitoringEventType.SECURITY)
                            .eventCode(MonitoringEventCode.SEC_FORBIDDEN_ACCESS_ATTEMPT.name())
                            .severity(MonitoringSeverity.HIGH)
                            .title("Forbidden resource access attempt")
                            .message("User attempted to access resource without required authority")
                            .detailJson(MonitoringSecurityJsonUtil.buildDetailJson(
                                    request,
                                    "requiredRole",
                                    "ADMIN"))
                            .sourceClass(getClass().getSimpleName())
                            .sourceMethod("handle")
                            .build(),
                    request);
        } catch (Exception e) {
            log.error("Failed to write access denied monitoring event", e);
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":403,\"message\":\"Forbidden\"}");
    }
}
