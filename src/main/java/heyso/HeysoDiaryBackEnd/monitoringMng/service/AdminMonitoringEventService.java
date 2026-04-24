package heyso.HeysoDiaryBackEnd.monitoringMng.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver.ResolvedModel;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver.BindingResolution;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import heyso.HeysoDiaryBackEnd.monitoring.model.MonitoringEvent;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDiagnoseResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventDetailResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventListResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventPageResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventResolutionResponse;
import heyso.HeysoDiaryBackEnd.monitoringMng.dto.MonitoringEventSearchRequest;
import heyso.HeysoDiaryBackEnd.monitoringMng.mapper.AdminMonitoringEventMapper;
import heyso.HeysoDiaryBackEnd.monitoringMng.utils.StackTraceUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminMonitoringEventService {

    private static final int SIMILAR_EVENT_LIMIT = 3;
    private static final String BINDING_DOMAIN = "MNT_DIAG";
    private static final String BINDING_FEATURE = "DIAGNOSE";

    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminMonitoringEventMapper adminMonitoringEventMapper;
    private final AiCallExecutor aiCallExecutor;
    private final AiPromptResolver aiPromptResolver;
    private final AiModelResolver aiModelResolver;

    @Transactional(readOnly = true)
    public MonitoringEventPageResponse getMonitoringEventPage(MonitoringEventSearchRequest request) {
        adminAuthorizationService.requireAdminUser();

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

    @Transactional(readOnly = true)
    public MonitoringEventDetailResponse getMonitoringEventDetail(Long eventId) {
        adminAuthorizationService.requireAdminUser();

        MonitoringEventDetailResponse detail = adminMonitoringEventMapper.selectMonitoringEventDetail(eventId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitoring event not found");
        }
        return detail;
    }

    @Transactional(readOnly = true)
    public MonitoringEventDiagnoseResponse diagnoseMonitoringEvent(Long eventId) {
        adminAuthorizationService.requireAdminUser();

        MonitoringEventDetailResponse detail = adminMonitoringEventMapper.selectMonitoringEventDetail(eventId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitoring event not found");
        }

        List<MonitoringEventDetailResponse> similarEvents = adminMonitoringEventMapper.selectSimilarMonitoringEvents(
                detail.getTitle(),
                detail.getMessage(),
                eventId,
                SIMILAR_EVENT_LIMIT);

        Map<String, String> variables = Map.of(
                "current_event", formatEventBlock(detail),
                "similar_events", formatSimilarEventsBlock(similarEvents));
        BindingResolution resolution = aiPromptResolver.resolve(BINDING_DOMAIN, BINDING_FEATURE, variables);
        ResolvedModel resolvedModel = aiModelResolver.resolve(resolution.profile());

        String diagnosis = callDiagnosisAi(detail, resolution, resolvedModel).content();
        if (StringUtils.isBlank(diagnosis)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned empty assistant content");
        }

        return MonitoringEventDiagnoseResponse.builder()
                .diagnosis(diagnosis.trim())
                .build();
    }

    @Transactional
    public MonitoringEventResolutionResponse updateResolution(MonitoringEventResolutionRequest request) {
        User user = adminAuthorizationService.requireAdminUser();
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

    private AiResponse callDiagnosisAi(MonitoringEventDetailResponse detail,
            BindingResolution resolution,
            ResolvedModel resolvedModel) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(new AiMessage("system", resolution.renderedSystemPrompt()));
        messages.add(new AiMessage("user", resolution.renderedUserPrompt()));

        // 이 메서드는 monitoring 이벤트를 AI로 진단하는 기능이다.
        // AI 호출 실패 시 monitoring 테이블에 재기록하면 무한 재귀 루프 위험이 있으므로,
        // 의도적으로 MonitoringEventService를 호출하지 않고 SLF4J 로그만 남긴다.
        try {
            Double temperature = resolution.profile().getTemperature() == null
                    ? null
                    : resolution.profile().getTemperature().doubleValue();
            Double topP = resolution.profile().getTopP() == null
                    ? null
                    : resolution.profile().getTopP().doubleValue();

            return aiCallExecutor.call(AiRequest.builder()
                    .provider(resolvedModel.provider())
                    .model(resolvedModel.model())
                    .messages(messages)
                    .temperature(temperature)
                    .topP(topP)
                    .maxTokens(resolution.profile().getMaxTokens())
                    .build());
        } catch (Exception e) {
            log.error("AI diagnosis call failed. eventId={}, model={}, message={}",
                    detail.getEventId(), resolvedModel.model(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI request failed");
        }
    }

    private String formatEventBlock(MonitoringEventDetailResponse event) {
        StringBuilder prompt = new StringBuilder();
        appendEventBlock(prompt, event);
        return prompt.toString();
    }

    private String formatSimilarEventsBlock(List<MonitoringEventDetailResponse> similarEvents) {
        if (similarEvents == null || similarEvents.isEmpty()) {
            return "없음";
        }

        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < similarEvents.size(); i++) {
            if (i > 0) {
                prompt.append('\n');
            }
            prompt.append('(').append(i + 1).append(")\n");
            appendEventBlock(prompt, similarEvents.get(i));
        }
        return prompt.toString();
    }

    private void appendEventBlock(StringBuilder prompt, MonitoringEventDetailResponse event) {
        prompt.append("eventId: ").append(event.getEventId()).append('\n');
        prompt.append("createdAt: ").append(valueOrDash(event.getCreatedAt())).append('\n');
        prompt.append("eventType: ").append(valueOrDash(event.getEventType())).append('\n');
        prompt.append("severity: ").append(valueOrDash(event.getSeverity())).append('\n');
        prompt.append("eventCode: ").append(valueOrDash(event.getEventCode())).append('\n');
        prompt.append("title: ").append(valueOrDash(event.getTitle())).append('\n');
        prompt.append("message: ").append(valueOrDash(event.getMessage())).append('\n');
        prompt.append("exceptionClass: ").append(valueOrDash(event.getExceptionClass())).append('\n');
        prompt.append("exceptionMessage: ").append(valueOrDash(event.getExceptionMessage())).append('\n');
        prompt.append("sourceClass: ").append(valueOrDash(event.getSourceClass())).append('\n');
        prompt.append("sourceMethod: ").append(valueOrDash(event.getSourceMethod())).append('\n');
        prompt.append("requestUri: ").append(valueOrDash(event.getRequestUri())).append('\n');
        prompt.append("httpMethod: ").append(valueOrDash(event.getHttpMethod())).append('\n');
        prompt.append("traceId: ").append(valueOrDash(event.getTraceId())).append('\n');
        prompt.append("detailJson: ").append(valueOrDash(event.getDetailJson())).append('\n');
        prompt.append("stackTrace:\n").append(StackTraceUtils.summarize(event.getStackTrace())).append('\n');
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
