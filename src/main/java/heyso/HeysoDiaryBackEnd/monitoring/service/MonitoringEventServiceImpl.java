package heyso.HeysoDiaryBackEnd.monitoring.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.monitoring.dto.MonitoringEventCreateCommand;
import heyso.HeysoDiaryBackEnd.monitoring.mapper.MonitoringEventMapper;
import heyso.HeysoDiaryBackEnd.monitoring.model.MonitoringEvent;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringContextExtractor;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventType;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringRequestContext;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringSeverity;
import heyso.HeysoDiaryBackEnd.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MonitoringEventServiceImpl implements MonitoringEventService {

    private static final String DEFAULT_TITLE = "Monitoring event";
    private static final String DEFAULT_UNEXPECTED_ERROR_TITLE = "Unexpected server error";
    private static final String DEFAULT_EVENT_CODE = "UNKNOWN_EVENT";

    private final MonitoringEventMapper monitoringEventMapper;
    private final MonitoringContextExtractor monitoringContextExtractor;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public MonitoringEventServiceImpl(
            MonitoringEventMapper monitoringEventMapper,
            MonitoringContextExtractor monitoringContextExtractor,
            PlatformTransactionManager transactionManager) {
        this.monitoringEventMapper = monitoringEventMapper;
        this.monitoringContextExtractor = monitoringContextExtractor;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void logError(String eventCode, String title, Throwable throwable, HttpServletRequest request) {
        try {
            MonitoringEvent event = buildErrorEvent(eventCode, title, throwable, request);
            insertMonitoringEventInNewTransaction(event);
        } catch (Exception e) {
            log.error("Failed to persist monitoring error event. eventCode={}", eventCode, e);
        }
    }

    @Override
    public void logEvent(MonitoringEventCreateCommand command, HttpServletRequest request) {
        try {
            MonitoringEvent event = buildManualEvent(command, request);
            insertMonitoringEventInNewTransaction(event);
        } catch (Exception e) {
            String eventCode = command != null ? command.getEventCode() : null;
            log.error("Failed to persist monitoring manual event. eventCode={}", eventCode, e);
        }
    }

    private MonitoringEvent buildErrorEvent(
            String eventCode,
            String title,
            Throwable throwable,
            HttpServletRequest request) {
        MonitoringRequestContext requestContext = monitoringContextExtractor.extract(request);
        User user = getCurrentUser();
        Throwable targetThrowable = throwable != null ? throwable : new RuntimeException("Unknown error");

        MonitoringEvent event = new MonitoringEvent();
        event.setEventType(MonitoringEventType.ERROR.name());
        event.setEventCode(isBlank(eventCode) ? MonitoringEventCode.SYS_UNEXPECTED_ERROR.name() : eventCode);
        event.setSeverity(MonitoringSeverity.HIGH.name());
        event.setTitle(isBlank(title) ? DEFAULT_UNEXPECTED_ERROR_TITLE : title);
        event.setMessage(defaultIfBlank(targetThrowable.getMessage(), DEFAULT_UNEXPECTED_ERROR_TITLE));
        event.setResolvedYn("N");

        applyRequestContext(event, requestContext);
        applyUserContext(event, user, null, null);
        applyThrowableContext(event, targetThrowable);
        return event;
    }

    private MonitoringEvent buildManualEvent(MonitoringEventCreateCommand command, HttpServletRequest request) {
        MonitoringRequestContext requestContext = monitoringContextExtractor.extract(request);
        User currentUser = getCurrentUser();

        MonitoringEvent event = new MonitoringEvent();
        event.setEventType(resolveEventType(command));
        event.setEventCode(resolveEventCode(command));
        event.setSeverity(resolveSeverity(command));
        event.setTitle(resolveTitle(command));
        event.setMessage(command != null ? command.getMessage() : null);
        event.setDetailJson(command != null ? command.getDetailJson() : null);
        event.setSourceClass(command != null ? command.getSourceClass() : null);
        event.setSourceMethod(command != null ? command.getSourceMethod() : null);
        event.setResolvedYn("N");

        applyRequestContext(event, requestContext);
        applyUserContext(
                event,
                currentUser,
                command != null ? command.getUserId() : null,
                command != null ? command.getUserRole() : null);
        return event;
    }

    private void insertMonitoringEventInNewTransaction(MonitoringEvent event) {
        requiresNewTransactionTemplate
                .executeWithoutResult(status -> monitoringEventMapper.insertMonitoringEvent(event));
    }

    private void applyRequestContext(MonitoringEvent event, MonitoringRequestContext requestContext) {
        if (requestContext == null) {
            return;
        }
        event.setHttpMethod(requestContext.getHttpMethod());
        event.setRequestUri(requestContext.getRequestUri());
        event.setQueryString(requestContext.getQueryString());
        event.setClientIp(requestContext.getClientIp());
        event.setUserAgent(requestContext.getUserAgent());
        event.setSessionId(requestContext.getSessionId());
        event.setTraceId(requestContext.getTraceId());
    }

    private void applyUserContext(MonitoringEvent event, User currentUser, Long commandUserId, String commandUserRole) {
        event.setUserId(commandUserId != null ? commandUserId : (currentUser != null ? currentUser.getUserId() : null));
        event.setUserRole(defaultIfBlank(commandUserRole, currentUser != null ? currentUser.getRole() : null));
    }

    private void applyThrowableContext(MonitoringEvent event, Throwable throwable) {
        event.setExceptionClass(throwable.getClass().getName());
        event.setExceptionMessage(throwable.getMessage());
        event.setStackTrace(convertStackTraceToString(throwable));

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            event.setSourceClass(stackTrace[0].getClassName());
            event.setSourceMethod(stackTrace[0].getMethodName());
        }
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser().orElse(null);
    }

    private String convertStackTraceToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            return stringWriter.toString();
        }
    }

    private String resolveEventType(MonitoringEventCreateCommand command) {
        if (command == null || command.getEventType() == null) {
            return MonitoringEventType.INFO.name();
        }
        return command.getEventType().name();
    }

    private String resolveEventCode(MonitoringEventCreateCommand command) {
        if (command == null || isBlank(command.getEventCode())) {
            return DEFAULT_EVENT_CODE;
        }
        return command.getEventCode();
    }

    private String resolveSeverity(MonitoringEventCreateCommand command) {
        if (command == null || command.getSeverity() == null) {
            return MonitoringSeverity.MEDIUM.name();
        }
        return command.getSeverity().name();
    }

    private String resolveTitle(MonitoringEventCreateCommand command) {
        if (command == null || isBlank(command.getTitle())) {
            return DEFAULT_TITLE;
        }
        return command.getTitle();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
