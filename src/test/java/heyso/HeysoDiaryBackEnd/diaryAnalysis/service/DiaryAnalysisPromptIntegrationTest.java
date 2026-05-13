package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;

@SpringBootTest
@ActiveProfiles("local")
class DiaryAnalysisPromptIntegrationTest {
    @Autowired
    private AiPromptResolver aiPromptResolver;

    @Test
    @DisplayName("DIARY_ANALYSIS STRUCTURED_ANALYSIS binding을 resolve한다")
    void resolveDiaryAnalysisPrompt() {
        Map<String, String> variables = new HashMap<>();
        variables.put("diary_id", "1");
        variables.put("user_id", "1");
        variables.put("diary_date", "2026-05-13");
        variables.put("diary_updated_at", "2026-05-13T12:00:00");
        variables.put("title", "테스트 일기");
        variables.put("mood_id", "CALM");
        variables.put("tags_json", "[\"test\"]");
        variables.put("content_md", "오늘은 마음을 정리했다.");
        variables.put("event_type_codes_json", "[\"DAILY_LIFE\"]");
        variables.put("emotion_codes_json", "[\"CALM\"]");
        variables.put("trait_definitions_json", "[{\"traitKey\":\"SELF_REFLECTION\"}]");

        AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(
                "DIARY_ANALYSIS",
                "STRUCTURED_ANALYSIS",
                variables);

        assertThat(resolution.binding().getDomainType()).isEqualTo("DIARY_ANALYSIS");
        assertThat(resolution.binding().getFeatureKey()).isEqualTo("STRUCTURED_ANALYSIS");
        assertThat(resolution.profile().getRuntimeProfileId()).isNotNull();
        assertThat(resolution.renderedSystemPrompt()).contains("JSON object");
        assertThat(resolution.renderedUserPrompt()).contains("오늘은 마음을 정리했다.");
    }
}
