package heyso.HeysoDiaryBackEnd.monitoring.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import heyso.HeysoDiaryBackEnd.userMng.exception.AdminUserConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionLoggingHandler {

    private final MonitoringEventService monitoringEventService;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", exception.getStatusCode().value());
        response.put("message", exception.getReason());
        if (exception instanceof AdminUserConflictException adminUserConflictException) {
            response.put("errorCode", adminUserConflictException.getErrorCode());
        }
        return ResponseEntity.status(exception.getStatusCode()).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        log.warn("No handler/static resource found. method={}, uri={}", request.getMethod(), request.getRequestURI());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception, HttpServletRequest request) {
        monitoringEventService.logError(
                MonitoringEventCode.SYS_UNEXPECTED_ERROR.name(),
                "Unexpected server error",
                exception,
                request);

        log.error("Unhandled exception", exception);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "Unexpected server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
