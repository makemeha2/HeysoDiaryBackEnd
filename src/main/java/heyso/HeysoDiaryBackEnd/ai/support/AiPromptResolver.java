package heyso.HeysoDiaryBackEnd.ai.support;

import java.util.Map;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptBindingMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiRuntimeProfileMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptBinding;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPromptResolver {

    private final AiPromptBindingMapper aiPromptBindingMapper;
    private final AiRuntimeProfileMapper aiRuntimeProfileMapper;
    private final AiTemplateRenderer aiTemplateRenderer;

    public record BindingResolution(
            AiPromptBinding binding,
            AiRuntimeProfile profile,
            String renderedSystemPrompt,
            String renderedUserPrompt
    ) {}

    public BindingResolution resolve(String domainType, String featureKey, Map<String, String> variables) {
        AiPromptBinding binding = aiPromptBindingMapper.selectByDomainAndFeature(domainType, featureKey);
        if (binding == null) {
            throw new RuntimeException("Active binding not found for domainType=" + domainType + ", featureKey=" + featureKey);
        }

        AiRuntimeProfile profile = aiRuntimeProfileMapper.selectById(binding.getRuntimeProfileId());
        if (profile == null) {
            throw new RuntimeException("RuntimeProfile not found: " + binding.getRuntimeProfileId());
        }

        String renderedSystemPrompt = aiTemplateRenderer.render(binding.getSystemTemplateId(), variables);
        String renderedUserPrompt = aiTemplateRenderer.render(binding.getUserTemplateId(), variables);

        log.info(":::: renderedUserPrompt : {}", renderedUserPrompt);

        return new BindingResolution(binding, profile, renderedSystemPrompt, renderedUserPrompt);
    }
}
