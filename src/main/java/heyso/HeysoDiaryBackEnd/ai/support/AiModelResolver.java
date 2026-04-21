package heyso.HeysoDiaryBackEnd.ai.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.comCd.mapper.CommonCodeMapper;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiModelResolver {

    private static final String AI_MODELS_GROUP_ID = "AI_MODELS";

    private final CommonCodeMapper commonCodeMapper;

    public record ResolvedModel(String model, AiProvider provider) {}

    public ResolvedModel resolve(AiRuntimeProfile profile) {
        if (profile == null) {
            throw new IllegalStateException("AiRuntimeProfile is required");
        }

        CommonCode commonCode = commonCodeMapper.selectCommonCodeById(AI_MODELS_GROUP_ID, profile.getModel());
        String resolvedModel = resolveModel(profile, commonCode);
        AiProvider resolvedProvider = resolveProvider(profile, commonCode);

        return new ResolvedModel(resolvedModel, resolvedProvider);
    }

    private String resolveModel(AiRuntimeProfile profile, CommonCode commonCode) {
        String model = commonCode != null && StringUtils.isNotBlank(commonCode.getCodeName())
                ? commonCode.getCodeName()
                : profile.getModel();

        if (StringUtils.isBlank(model)) {
            throw new IllegalStateException("Resolved model is blank. profileKey=" + profile.getProfileKey());
        }

        return model;
    }

    private AiProvider resolveProvider(AiRuntimeProfile profile, CommonCode commonCode) {
        String provider = commonCode != null && StringUtils.isNotBlank(commonCode.getExtraInfo1())
                ? commonCode.getExtraInfo1()
                : profile.getProvider();

        if (StringUtils.isBlank(provider)) {
            throw new IllegalStateException("Resolved provider is blank. profileKey=" + profile.getProfileKey());
        }

        String normalized = provider.trim().toUpperCase();
        return switch (normalized) {
            case "OPENAI" -> AiProvider.OPENAI;
            case "ANTHROPIC", "CLAUDE" -> AiProvider.CLAUDE;
            default -> throw new IllegalStateException(
                    "Unsupported AI provider. profileKey=" + profile.getProfileKey() + ", provider=" + provider);
        };
    }
}
