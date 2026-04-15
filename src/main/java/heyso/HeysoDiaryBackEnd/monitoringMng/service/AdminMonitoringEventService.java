package heyso.HeysoDiaryBackEnd.monitoringMng.service;

import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDetailResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventPageResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventSearchRequest;

public interface AdminMonitoringEventService {
    MonitoringEventPageResponse getMonitoringEventPage(MonitoringEventSearchRequest request);

    MonitoringEventDetailResponse getMonitoringEventDetail(Long eventId);

    MonitoringEventResolutionResponse updateResolution(MonitoringEventResolutionRequest request);
}
