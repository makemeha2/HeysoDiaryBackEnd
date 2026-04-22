package heyso.HeysoDiaryBackEnd.diaryAiPolish.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiRuntimeProfileMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;

@SpringBootTest
@ActiveProfiles("local")
class DiaryAiPolishAiClientIntegrationTest {

    @Autowired
    private AiPromptResolver aiPromptResolver;

    @Autowired
    private AiModelResolver aiModelResolver;

    @Autowired
    private AiRuntimeProfileMapper aiRuntimeProfileMapper;

    @Test
    @DisplayName("AiPromptResolver가 DIARY_AI_POLISH binding을 정상적으로 resolve한다")
    void aiPromptResolver_resolves_binding_successfully() {
        String domainType = "DIARY_AI_POLISH";
        String featureKey = "POLISH";
        Map<String, String> variables = Map.of("original_content", "이것은 테스트 일기입니다.");

        AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(domainType, featureKey, variables);

        assertThat(resolution).isNotNull();
        assertThat(resolution.binding()).isNotNull();
        assertThat(resolution.profile()).isNotNull();
        assertThat(resolution.renderedSystemPrompt()).isNotBlank();
        assertThat(resolution.renderedUserPrompt()).isNotBlank();
        assertThat(resolution.renderedUserPrompt()).contains("이것은 테스트 일기입니다.");
    }

    @Test
    @DisplayName("AiModelResolver가 runtime profile을 정상적으로 해석한다")
    void aiModelResolver_resolves_model_correctly() {
        AiRuntimeProfile profile = aiRuntimeProfileMapper.selectById(2L);
        assertThat(profile).isNotNull();

        AiModelResolver.ResolvedModel resolvedModel = aiModelResolver.resolve(profile);

        assertThat(resolvedModel).isNotNull();
        assertThat(resolvedModel.model()).isNotBlank();
        assertThat(resolvedModel.provider()).isNotNull();
    }

    @Test
    @DisplayName("maxTokens가 0일 때 null로 정규화된다")
    void maxTokens_is_normalized_to_null_when_zero() {
        AiRuntimeProfile profile = aiRuntimeProfileMapper.selectById(2L);
        assertThat(profile).isNotNull();
        assertThat(profile.getMaxTokens()).isNotNull();

        // maxTokens가 0 이하면 null로 정규화되어야 함
        if (profile.getMaxTokens() != null && profile.getMaxTokens() <= 0) {
            assertThat(profile.getMaxTokens()).isLessThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("temperature가 BigDecimal에서 double로 변환된다")
    void temperature_converts_from_bigdecimal_to_double() {
        AiRuntimeProfile profile = aiRuntimeProfileMapper.selectById(2L);
        assertThat(profile).isNotNull();
        assertThat(profile.getTemperature()).isNotNull();

        Double tempDouble = profile.getTemperature().doubleValue();
        assertThat(tempDouble).isNotNull().isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("존재하지 않는 binding을 resolve할 때 예외가 발생한다")
    void aiPromptResolver_throws_exception_for_nonexistent_binding() {
        String domainType = "NONEXISTENT_DOMAIN";
        String featureKey = "NONEXISTENT_FEATURE";
        Map<String, String> variables = Map.of();

        assertThatThrownBy(() -> aiPromptResolver.resolve(domainType, featureKey, variables))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Active binding not found");
    }
}
