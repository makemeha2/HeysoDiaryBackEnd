package heyso.HeysoDiaryBackEnd.monitoring.service;

import heyso.HeysoDiaryBackEnd.monitoring.dto.MonitoringEventCreateCommand;
import jakarta.servlet.http.HttpServletRequest;

public interface MonitoringEventService {
    void logError(String eventCode, String title, Throwable throwable, HttpServletRequest request);

    void logEvent(MonitoringEventCreateCommand command, HttpServletRequest request);
}
