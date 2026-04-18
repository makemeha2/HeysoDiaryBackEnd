package heyso.HeysoDiaryBackEnd.monitoringMng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDetailResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventPageResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventSearchRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.service.AdminMonitoringEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/monitoring-events")
@RequiredArgsConstructor
public class AdminMonitoringEventController {

    private final AdminMonitoringEventService adminMonitoringEventService;

    @GetMapping
    public ResponseEntity<MonitoringEventPageResponse> getMonitoringEventPage(@Valid MonitoringEventSearchRequest request) {
        return ResponseEntity.ok(adminMonitoringEventService.getMonitoringEventPage(request));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<MonitoringEventDetailResponse> getMonitoringEventDetail(@PathVariable Long eventId) {
        return ResponseEntity.ok(adminMonitoringEventService.getMonitoringEventDetail(eventId));
    }

    @PatchMapping("/resolution")
    public ResponseEntity<MonitoringEventResolutionResponse> updateResolution(
            @Valid @RequestBody MonitoringEventResolutionRequest request) {
        return ResponseEntity.ok(adminMonitoringEventService.updateResolution(request));
    }
}
