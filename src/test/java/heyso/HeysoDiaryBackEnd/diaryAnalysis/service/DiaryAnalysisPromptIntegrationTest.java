package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.ActiveProfiles;

import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/db/seed/R__20260513_diary_analysis_prompt_seed.sql",
        config = @SqlConfig(encoding = "UTF-8"))
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
        variables.put("trait_definitions_json", "[\"SELF_REFLECTION\"]");

        AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(
                "DIARY_ANALYSIS",
                "STRUCTURED_ANALYSIS",
                variables);

        assertThat(resolution.binding().getDomainType()).isEqualTo("DIARY_ANALYSIS");
        assertThat(resolution.binding().getFeatureKey()).isEqualTo("STRUCTURED_ANALYSIS");
        assertThat(resolution.profile().getRuntimeProfileId()).isNotNull();
        assertThat(resolution.renderedSystemPrompt()).contains("JSON object");
        assertThat(resolution.renderedSystemPrompt())
                .contains("sum:", "evt_type:", "edc_txt:", "\"sum\"", "\"evts\"", "\"trait_edc\"");
        assertThat(resolution.renderedUserPrompt())
                .contains("title:", "mood_id: CALM", "\"test\"", "SELF_REFLECTION");
        assertThat(resolution.renderedUserPrompt())
                .doesNotContain("diary_id", "user_id", "diary_updated_at", "traitName", "traitDescription",
                        "traitCategory", "scoreDirection", "sortSeq");
    }

    @Test
    @DisplayName("DIARY_ANALYSIS seed prompt는 축약 응답 키와 축소 입력 변수만 사용한다")
    void diaryAnalysisSeedPromptUsesCompactContract() throws Exception {
        String seed = new ClassPathResource("db/seed/R__20260513_diary_analysis_prompt_seed.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(seed).contains("sum:", "evt_type:", "edc_txt:", "\"sum\"", "\"evts\"", "\"trait_edc\"");
        assertThat(seed).contains("traitKey 문자열 배열");
        assertThat(seed).contains("title: {{title}}", "mood_id: {{mood_id}}", "tags_json: {{tags_json}}",
                "{{content_md}}");
        assertThat(seed)
                .doesNotContain("diary_id: {{diary_id}}", "user_id: {{user_id}}",
                        "diary_updated_at: {{diary_updated_at}}", "traitName", "traitDescription", "traitCategory",
                        "scoreDirection", "sortSeq");
    }
}
