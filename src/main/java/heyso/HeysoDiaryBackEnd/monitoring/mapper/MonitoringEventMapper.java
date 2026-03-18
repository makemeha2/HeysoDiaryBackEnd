package heyso.HeysoDiaryBackEnd.monitoring.mapper;

import org.apache.ibatis.annotations.Mapper;

import heyso.HeysoDiaryBackEnd.monitoring.model.MonitoringEvent;

@Mapper
public interface MonitoringEventMapper {
    void insertMonitoringEvent(MonitoringEvent event);
}
