package heyso.HeysoDiaryBackEnd.monitoringMng.service;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
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
    private static final String DIAGNOSIS_MODEL = "claude-haiku-4-5-20251001";
    private static final String DIAGNOSIS_SYSTEM_PROMPT = """
            당신은 서버 에러 로그를 분석하는 전문가입니다. 제공된 로그를 보고 에러 발생 원인과 해결 방법을 한국어로 설명하세요.
            """;

    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminMonitoringEventMapper adminMonitoringEventMapper;
    private final AiCallExecutor aiCallExecutor;

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

        String diagnosis = callDiagnosisAi(detail, similarEvents).content();
        if (StringUtils.isBlank(diagnosis)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Claude returned empty assistant content");
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
            List<MonitoringEventDetailResponse> similarEvents) {
        String model = DIAGNOSIS_MODEL;
        List<AiMessage> messages = new ArrayList<>();
        messages.add(new AiMessage("system", DIAGNOSIS_SYSTEM_PROMPT));
        messages.add(new AiMessage("user", buildDiagnosisUserPrompt(detail, similarEvents)));

        try {
            return aiCallExecutor.call(AiRequest.builder()
                    .provider(AiProvider.CLAUDE)
                    .model(model)
                    .messages(messages)
                    .temperature(0.3)
                    .maxTokens(3000)
                    .build());
        } catch (NonTransientAiException e) {
            if (isAuthenticationFailure(e)) {
                log.error("Claude diagnosis authentication failure. eventId={}, model={}, message={}",
                        detail.getEventId(), model, e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Claude authentication failed. Check CLAUDE_API_KEY and Anthropic credentials.");
            }

            log.error("Claude diagnosis non-retryable AI failure. eventId={}, model={}, message={}",
                    detail.getEventId(), model, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Claude request failed due to a non-retryable AI error.");
        } catch (ResourceAccessException e) {
            if (hasCause(e, UnknownHostException.class)) {
                log.error("Claude diagnosis DNS resolution failure. eventId={}, host=api.anthropic.com, message={}",
                        detail.getEventId(), e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Claude request failed due to DNS resolution. Check network or DNS settings.");
            }

            if (hasCause(e, SocketTimeoutException.class)) {
                log.error("Claude diagnosis timeout. eventId={}, model={}, message={}",
                        detail.getEventId(), model, e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Claude request timed out. Please retry.");
            }

            log.error("Claude diagnosis network access failure. eventId={}, model={}, message={}",
                    detail.getEventId(), model, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Claude request failed due to a network access error.");
        } catch (Exception e) {
            log.error("Claude diagnosis unexpected failure. eventId={}, model={}, message={}",
                    detail.getEventId(), model, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Claude request failed: " + e.getMessage());
        }
    }

    private boolean isAuthenticationFailure(Throwable throwable) {
        String message = throwable == null ? null : throwable.getMessage();
        if (StringUtils.isBlank(message)) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("401")
                || normalized.contains("authentication_error")
                || normalized.contains("x-api-key")
                || normalized.contains("api key")
                || normalized.contains("unauthorized");
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildDiagnosisUserPrompt(MonitoringEventDetailResponse detail,
            List<MonitoringEventDetailResponse> similarEvents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[현재 이벤트]\n");
        appendEventBlock(prompt, detail);

        if (similarEvents == null || similarEvents.isEmpty()) {
            prompt.append("\n[유사 이벤트]\n없음\n");
        } else {
            prompt.append("\n[유사 이벤트 목록]\n");
            for (int i = 0; i < similarEvents.size(); i++) {
                prompt.append("\n(").append(i + 1).append(")\n");
                appendEventBlock(prompt, similarEvents.get(i));
            }
        }

        prompt.append("""

                위 정보를 바탕으로 아래 형식에 맞춰 한국어로 답변하세요.
                1. 추정 원인
                2. 확인이 필요한 포인트
                3. 해결 방법 또는 대응 방안
                """);
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
