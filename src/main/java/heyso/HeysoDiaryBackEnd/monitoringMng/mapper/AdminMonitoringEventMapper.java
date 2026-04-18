package heyso.HeysoDiaryBackEnd.monitoringMng.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.monitoring.model.MonitoringEvent;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDetailResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventListResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventSearchRequest;

@Mapper
public interface AdminMonitoringEventMapper {
    List<MonitoringEventListResponse> selectMonitoringEventPage(@Param("request") MonitoringEventSearchRequest request);

    long countMonitoringEvents(@Param("request") MonitoringEventSearchRequest request);

    MonitoringEventDetailResponse selectMonitoringEventDetail(@Param("eventId") Long eventId);

    List<MonitoringEvent> selectMonitoringEventsByIds(@Param("eventIds") List<Long> eventIds);

    int updateMonitoringEventResolution(
            @Param("eventId") Long eventId,
            @Param("resolvedYn") String resolvedYn,
            @Param("resolvedBy") Long resolvedBy);
}
