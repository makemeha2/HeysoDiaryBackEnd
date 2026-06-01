package heyso.HeysoDiaryBackEnd.userMemorySnapshot.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.type.UserMemorySnapshotException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMemorySnapshotAiClient {
    static final String DOMAIN_TYPE = "USER_MEMORY";
    static final String FEATURE_KEY = "SNAPSHOT_SUMMARY";

    private final AiPromptResolver aiPromptResolver;
    private final AiModelResolver aiModelResolver;
    private final AiCallExecutor aiCallExecutor;
    private final MonitoringEventService monitoringEventService;

    public ResolvedSnapshotPrompt resolve(Map<String, String> variables) {
        try {
            AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(DOMAIN_TYPE, FEATURE_KEY,
                    variables);
            AiModelResolver.ResolvedModel resolvedModel = aiModelResolver.resolve(resolution.profile());
            return new ResolvedSnapshotPrompt(resolution, resolvedModel);
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "USER_MEMORY prompt resolution failed", e, null);
            throw new UserMemorySnapshotException("USER_MEMORY prompt binding resolution failed", e);
        }
    }

    public String execute(ResolvedSnapshotPrompt prompt) {
        AiRuntimeProfile profile = prompt.bindingResolution().profile();
        List<AiMessage> messages = List.of(
                new AiMessage("system", prompt.bindingResolution().renderedSystemPrompt()),
                new AiMessage("user", prompt.bindingResolution().renderedUserPrompt()));
        try {
            AiResponse response = aiCallExecutor.call(AiRequest.builder()
                    .provider(prompt.resolvedModel().provider())
                    .model(prompt.resolvedModel().model())
                    .messages(messages)
                    .temperature(profile.getTemperature() != null ? profile.getTemperature().doubleValue() : null)
                    .topP(profile.getTopP() != null ? profile.getTopP().doubleValue() : null)
                    .maxTokens(normalizeMaxTokens(profile.getMaxTokens()))
                    .build());
            if (response == null || StringUtils.isBlank(response.content())) {
                throw new UserMemorySnapshotException("AI returned empty user memory snapshot response");
            }
            return response.content().trim();
        } catch (UserMemorySnapshotException e) {
            throw e;
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "USER_MEMORY call failed", e, null);
            throw new UserMemorySnapshotException("USER_MEMORY AI call failed", e);
        }
    }

    private Integer normalizeMaxTokens(Integer maxTokens) {
        return maxTokens == null || maxTokens <= 0 ? null : maxTokens;
    }

    public record ResolvedSnapshotPrompt(
            AiPromptResolver.BindingResolution bindingResolution,
            AiModelResolver.ResolvedModel resolvedModel) {
    }
}
