package heyso.HeysoDiaryBackEnd.diaryAnalysis.support;

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
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisErrorCode;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisException;
import heyso.HeysoDiaryBackEnd.monitoring.service.MonitoringEventService;
import heyso.HeysoDiaryBackEnd.monitoring.support.MonitoringEventCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAnalysisAiClient {
    static final String DOMAIN_TYPE = "DIARY_ANALYSIS";
    static final String FEATURE_KEY = "STRUCTURED_ANALYSIS";

    private final AiPromptResolver aiPromptResolver;
    private final AiModelResolver aiModelResolver;
    private final AiCallExecutor aiCallExecutor;
    private final MonitoringEventService monitoringEventService;

    public ResolvedAnalysisPrompt resolve(Map<String, String> variables) {
        try {
            AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(DOMAIN_TYPE, FEATURE_KEY,
                    variables);
            AiModelResolver.ResolvedModel resolvedModel = aiModelResolver.resolve(resolution.profile());
            return new ResolvedAnalysisPrompt(resolution, resolvedModel);
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "DIARY_ANALYSIS prompt resolution failed", e, null);
            throw new DiaryAnalysisException(DiaryAnalysisErrorCode.PROMPT_BINDING_NOT_FOUND,
                    "DIARY_ANALYSIS prompt binding resolution failed", e);
        }
    }

    public String execute(ResolvedAnalysisPrompt prompt) {
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
                throw new DiaryAnalysisException(DiaryAnalysisErrorCode.AI_EMPTY_RESPONSE,
                        "AI returned empty diary analysis response");
            }
            return response.content().trim();
        } catch (DiaryAnalysisException e) {
            throw e;
        } catch (Exception e) {
            monitoringEventService.logError(MonitoringEventCode.AI_CALL_FAIL.name(),
                    "DIARY_ANALYSIS call failed", e, null);
            throw new DiaryAnalysisException(DiaryAnalysisErrorCode.AI_CALL_FAILED,
                    "DIARY_ANALYSIS AI call failed", e);
        }
    }

    private Integer normalizeMaxTokens(Integer maxTokens) {
        return maxTokens == null || maxTokens <= 0 ? null : maxTokens;
    }

    public record ResolvedAnalysisPrompt(
            AiPromptResolver.BindingResolution bindingResolution,
            AiModelResolver.ResolvedModel resolvedModel) {
    }
}
