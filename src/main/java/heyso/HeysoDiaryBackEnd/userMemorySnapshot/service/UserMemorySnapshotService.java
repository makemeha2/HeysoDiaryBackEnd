package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.mapper.UserMemorySnapshotMapper;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshot;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotEventSource;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotParsedResponse;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotProfileSource;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotRebuildResult;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotAiClient;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotAiClient.ResolvedSnapshotPrompt;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotResponseParser;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.type.UserMemorySnapshotException;
import heyso.HeysoDiaryBackEnd.utils.JsonHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMemorySnapshotService {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final int SOURCE_WINDOW_DAYS = 90;

    private final UserMemorySnapshotMapper userMemorySnapshotMapper;
    private final UserMemorySnapshotAiClient aiClient;
    private final UserMemorySnapshotResponseParser responseParser;
    private final UserMemorySnapshotPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<Long> getChangedUserIds() {
        return userMemorySnapshotMapper.selectChangedUserIds();
    }

    public AdminBatchRunResult rebuildChangedUserSnapshots() {
        List<Long> userIds = getChangedUserIds();
        if (userIds.isEmpty()) {
            return new AdminBatchRunResult(0, 0, "memory snapshot 생성 대상 사용자가 없습니다.");
        }

        LocalDate sourceToDate = LocalDate.now(SEOUL_ZONE);
        LocalDate sourceFromDate = sourceToDate.minusDays(SOURCE_WINDOW_DAYS - 1L);
        int successCount = 0;
        int failureCount = 0;
        int createdSnapshotCount = 0;
        int skippedCount = 0;

        log.info("User memory snapshot batch started. userCount={}", userIds.size());
        for (Long userId : userIds) {
            try {
                UserMemorySnapshotRebuildResult result = rebuildUserSnapshot(userId, sourceFromDate, sourceToDate);
                successCount++;
                createdSnapshotCount += result.createdSnapshotCount();
                skippedCount += result.skippedCount();
            } catch (Exception e) {
                failureCount++;
                log.error("User memory snapshot rebuild failed. userId={}", userId, e);
            }
        }
        log.info("User memory snapshot batch finished. userCount={}, successCount={}, failureCount={}, "
                + "createdSnapshotCount={}, skippedCount={}",
                userIds.size(), successCount, failureCount, createdSnapshotCount, skippedCount);

        return new AdminBatchRunResult(
                successCount,
                failureCount,
                "처리 대상 " + userIds.size()
                        + "명 중 성공 " + successCount
                        + "명, 실패 " + failureCount
                        + "명, 생성 snapshot " + createdSnapshotCount
                        + "건, 건너뜀 " + skippedCount + "건");
    }

    public UserMemorySnapshotRebuildResult rebuildUserSnapshot(Long userId, LocalDate sourceFromDate,
            LocalDate sourceToDate) {
        List<UserMemorySnapshotEventSource> events = userMemorySnapshotMapper.selectActiveEventSources(
                userId, sourceFromDate, sourceToDate);
        List<UserMemorySnapshotProfileSource> profiles = userMemorySnapshotMapper.selectActiveProfileSources(userId);
        if (events.isEmpty() && profiles.isEmpty()) {
            return new UserMemorySnapshotRebuildResult(0, 1);
        }

        SnapshotSourceInput sourceInput = buildSourceInput(userId, sourceFromDate, sourceToDate, events, profiles);
        ResolvedSnapshotPrompt prompt = aiClient.resolve(sourceInput.variables());
        String content = aiClient.execute(prompt);
        UserMemorySnapshotParsedResponse parsed = responseParser.parse(content);
        UserMemorySnapshot snapshot = toSnapshot(userId, sourceFromDate, sourceToDate, sourceInput, prompt, parsed);
        persistenceService.replaceActiveSnapshot(snapshot);
        return new UserMemorySnapshotRebuildResult(1, 0);
    }

    private SnapshotSourceInput buildSourceInput(Long userId, LocalDate sourceFromDate, LocalDate sourceToDate,
            List<UserMemorySnapshotEventSource> events, List<UserMemorySnapshotProfileSource> profiles) {
        String eventsJson = renderSourceJson(events);
        String profilesJson = renderSourceJson(profiles);
        String sourceHash = JsonHashUtil.sha256Hex(eventsJson + "\n" + profilesJson + "\n" + sourceFromDate + "\n"
                + sourceToDate, e -> new UserMemorySnapshotException("Failed to build user memory snapshot source hash",
                        e));

        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("source_from_date", sourceFromDate.toString());
        variables.put("source_to_date", sourceToDate.toString());
        variables.put("events_json", eventsJson);
        variables.put("trait_profiles_json", profilesJson);

        Map<String, Object> sourceMeta = new LinkedHashMap<>();
        sourceMeta.put("source_from_date", sourceFromDate.toString());
        sourceMeta.put("source_to_date", sourceToDate.toString());
        sourceMeta.put("event_count", events.size());
        sourceMeta.put("profile_count", profiles.size());
        sourceMeta.put("latest_source_updated_at", latestUpdatedAt(events, profiles));
        sourceMeta.put("source_hash", sourceHash);
        sourceMeta.put("user_id", userId);

        return new SnapshotSourceInput(variables, sourceMeta);
    }

    private UserMemorySnapshot toSnapshot(Long userId, LocalDate sourceFromDate, LocalDate sourceToDate,
            SnapshotSourceInput sourceInput, ResolvedSnapshotPrompt prompt, UserMemorySnapshotParsedResponse parsed) {
        AiPromptResolver.BindingResolution bindingResolution = prompt.bindingResolution();
        Map<String, Object> sourceMeta = new LinkedHashMap<>(sourceInput.sourceMeta());
        sourceMeta.put("binding_id", bindingResolution.binding().getBindingId());
        sourceMeta.put("system_template_id", bindingResolution.binding().getSystemTemplateId());
        sourceMeta.put("user_template_id", bindingResolution.binding().getUserTemplateId());
        sourceMeta.put("runtime_profile_id", bindingResolution.binding().getRuntimeProfileId());

        UserMemorySnapshot snapshot = new UserMemorySnapshot();
        snapshot.setUserId(userId);
        snapshot.setSummaryText(parsed.summaryText());
        snapshot.setRecurringThemesJson(parsed.recurringThemesJson());
        snapshot.setImportantPeopleJson(parsed.importantPeopleJson());
        snapshot.setStressFactorsJson(parsed.stressFactorsJson());
        snapshot.setRecoveryFactorsJson(parsed.recoveryFactorsJson());
        snapshot.setTraitSummaryJson(parsed.traitSummaryJson());
        snapshot.setSourceFromDate(sourceFromDate);
        snapshot.setSourceToDate(sourceToDate);
        snapshot.setSourceJson(renderSourceJson(sourceMeta));
        return snapshot;
    }

    private String latestUpdatedAt(List<UserMemorySnapshotEventSource> events,
            List<UserMemorySnapshotProfileSource> profiles) {
        return java.util.stream.Stream.concat(
                events.stream().map(UserMemorySnapshotEventSource::getUpdatedAt),
                profiles.stream().map(UserMemorySnapshotProfileSource::getUpdatedAt))
                .filter(java.util.Objects::nonNull)
                .max(java.time.LocalDateTime::compareTo)
                .map(java.time.LocalDateTime::toString)
                .orElse(null);
    }

    private String renderSourceJson(Object value) {
        return JsonHashUtil.toJson(objectMapper, value,
                e -> new UserMemorySnapshotException("Failed to render user memory snapshot source JSON", e));
    }

    private record SnapshotSourceInput(
            Map<String, String> variables,
            Map<String, Object> sourceMeta) {
    }
}
