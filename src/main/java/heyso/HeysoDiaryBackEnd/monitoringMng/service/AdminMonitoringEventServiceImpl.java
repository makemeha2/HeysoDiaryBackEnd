package heyso.HeysoDiaryBackEnd.monitoringMng.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.monitoring.model.MonitoringEvent;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDetailResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventListResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventPageResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventSearchRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.mapper.AdminMonitoringEventMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMonitoringEventServiceImpl implements AdminMonitoringEventService {

    private static final String ADMIN_SCOPE_AUTHORITY = "SCOPE_admin";

    private final AdminMonitoringEventMapper adminMonitoringEventMapper;

    @Override
    @Transactional(readOnly = true)
    public MonitoringEventPageResponse getMonitoringEventPage(MonitoringEventSearchRequest request) {
        requireAdminUser();

        List<MonitoringEventListResponse> items = adminMonitoringEventMapper.selectMonitoringEventPage(request);
        long totalCount = adminMonitoringEventMapper.countMonitoringEvents(request);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / request.getSize());

        return MonitoringEventPageResponse.builder()
                .items(items)
                .page(request.getPage())
                .size(request.getSize())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonitoringEventDetailResponse getMonitoringEventDetail(Long eventId) {
        requireAdminUser();

        MonitoringEventDetailResponse detail = adminMonitoringEventMapper.selectMonitoringEventDetail(eventId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitoring event not found");
        }
        return detail;
    }

    @Override
    @Transactional
    public MonitoringEventResolutionResponse updateResolution(MonitoringEventResolutionRequest request) {
        User user = requireAdminUser();
        List<Long> eventIds = request.getEventIds();
        List<MonitoringEvent> events = eventIds.isEmpty()
                ? Collections.emptyList()
                : adminMonitoringEventMapper.selectMonitoringEventsByIds(eventIds);

        Map<Long, MonitoringEvent> eventMap = new HashMap<>();
        for (MonitoringEvent event : events) {
            eventMap.put(event.getEventId(), event);
        }

        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (Long eventId : eventIds) {
            MonitoringEvent event = eventMap.get(eventId);
            if (event == null) {
                failedCount++;
                continue;
            }

            if (request.getResolvedYn().equals(event.getResolvedYn())) {
                skippedCount++;
                continue;
            }

            int updated = adminMonitoringEventMapper.updateMonitoringEventResolution(
                    eventId,
                    request.getResolvedYn(),
                    "Y".equals(request.getResolvedYn()) ? user.getUserId() : null);
            if (updated > 0) {
                successCount++;
                event.setResolvedYn(request.getResolvedYn());
            } else {
                failedCount++;
            }
        }

        return MonitoringEventResolutionResponse.builder()
                .requestedCount(eventIds.size())
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(failedCount)
                .build();
    }

    private User requireAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminScope = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> ADMIN_SCOPE_AUTHORITY.equals(authority.getAuthority()));
        if (!hasAdminScope) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin scope required");
        }

        User user = SecurityUtils.getCurrentUserOrThrow();
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user;
    }
}
