package heyso.HeysoDiaryBackEnd.ai.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.comCd.mapper.CommonCodeMapper;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;

@ExtendWith(MockitoExtension.class)
class AiModelResolverTest {

    @Mock
    private CommonCodeMapper commonCodeMapper;

    @InjectMocks
    private AiModelResolver aiModelResolver;

    @Test
    @DisplayName("AI_MODELS 공통코드가 있으면 codeName과 extraInfo1으로 실제 모델과 provider를 해석한다")
    void resolve_usesCommonCodeMapping_whenCodeExists() {
        AiRuntimeProfile profile = profile("MNT_DIAG_DEFAULT", "Anthropic", "ATRP_002");
        CommonCode commonCode = CommonCode.builder()
                .groupId("AI_MODELS")
                .codeId("ATRP_002")
                .codeName("claude-haiku-4-5-20251001")
                .extraInfo1("Anthropic")
                .build();

        when(commonCodeMapper.selectCommonCodeById("AI_MODELS", "ATRP_002")).thenReturn(commonCode);

        AiModelResolver.ResolvedModel resolved = aiModelResolver.resolve(profile);

        assertThat(resolved.model()).isEqualTo("claude-haiku-4-5-20251001");
        assertThat(resolved.provider()).isEqualTo(AiProvider.CLAUDE);
    }

    @Test
    @DisplayName("공통코드가 없으면 profile의 model/provider 값을 그대로 fallback 한다")
    void resolve_fallsBackToProfileValues_whenCommonCodeDoesNotExist() {
        AiRuntimeProfile profile = profile("DIARY_AI_DEFAULT", "OpenAI", "gpt-4o");

        when(commonCodeMapper.selectCommonCodeById("AI_MODELS", "gpt-4o")).thenReturn(null);

        AiModelResolver.ResolvedModel resolved = aiModelResolver.resolve(profile);

        assertThat(resolved.model()).isEqualTo("gpt-4o");
        assertThat(resolved.provider()).isEqualTo(AiProvider.OPENAI);
    }

    @Test
    @DisplayName("공통코드 provider가 비어 있으면 profile provider로 fallback 한다")
    void resolve_fallsBackToProfileProvider_whenCommonCodeProviderIsBlank() {
        AiRuntimeProfile profile = profile("MNT_DIAG_DEFAULT", "Anthropic", "ATRP_002");
        CommonCode commonCode = CommonCode.builder()
                .groupId("AI_MODELS")
                .codeId("ATRP_002")
                .codeName("claude-haiku-4-5-20251001")
                .extraInfo1(" ")
                .build();

        when(commonCodeMapper.selectCommonCodeById("AI_MODELS", "ATRP_002")).thenReturn(commonCode);

        AiModelResolver.ResolvedModel resolved = aiModelResolver.resolve(profile);

        assertThat(resolved.model()).isEqualTo("claude-haiku-4-5-20251001");
        assertThat(resolved.provider()).isEqualTo(AiProvider.CLAUDE);
    }

    @Test
    @DisplayName("최종 모델명이 비어 있으면 IllegalStateException을 던진다")
    void resolve_throws_whenResolvedModelIsBlank() {
        AiRuntimeProfile profile = profile("BROKEN_PROFILE", "Anthropic", " ");

        when(commonCodeMapper.selectCommonCodeById("AI_MODELS", " ")).thenReturn(null);

        assertThatThrownBy(() -> aiModelResolver.resolve(profile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Resolved model is blank");
    }

    @Test
    @DisplayName("provider 매핑이 불가능하면 IllegalStateException을 던진다")
    void resolve_throws_whenProviderCannotBeMapped() {
        AiRuntimeProfile profile = profile("BROKEN_PROFILE", "Bedrock", "claude-3");

        when(commonCodeMapper.selectCommonCodeById("AI_MODELS", "claude-3")).thenReturn(null);

        assertThatThrownBy(() -> aiModelResolver.resolve(profile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported AI provider");
    }

    private AiRuntimeProfile profile(String profileKey, String provider, String model) {
        return AiRuntimeProfile.builder()
                .profileKey(profileKey)
                .provider(provider)
                .model(model)
                .build();
    }
}
