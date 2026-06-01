package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/db/seed/R__20260601_user_memory_snapshot_prompt_seed.sql",
        config = @SqlConfig(encoding = "UTF-8"))
class UserMemorySnapshotPromptIntegrationTest {
    @Autowired
    private AiPromptResolver aiPromptResolver;

    @Test
    @DisplayName("USER_MEMORY SNAPSHOT_SUMMARY binding을 resolve한다")
    void resolveUserMemorySnapshotPrompt() {
        Map<String, String> variables = new HashMap<>();
        variables.put("source_from_date", "2026-03-04");
        variables.put("source_to_date", "2026-06-01");
        variables.put("events_json", "[{\"event_summary\":\"친구와 대화했다\"}]");
        variables.put("trait_profiles_json", "[{\"trait_key\":\"SELF_REFLECTION\"}]");

        AiPromptResolver.BindingResolution resolution = aiPromptResolver.resolve(
                "USER_MEMORY",
                "SNAPSHOT_SUMMARY",
                variables);

        assertThat(resolution.binding().getDomainType()).isEqualTo("USER_MEMORY");
        assertThat(resolution.binding().getFeatureKey()).isEqualTo("SNAPSHOT_SUMMARY");
        assertThat(resolution.profile().getRuntimeProfileId()).isNotNull();
        assertThat(resolution.renderedSystemPrompt())
                .contains("JSON object", "\"summary\"", "\"recurring_themes\"", "\"trait_summary\"");
        assertThat(resolution.renderedUserPrompt())
                .contains("source_from_date: 2026-03-04", "친구와 대화했다", "SELF_REFLECTION");
    }

    @Test
    @DisplayName("USER_MEMORY seed prompt는 snapshot 저장 필드 계약을 포함한다")
    void userMemorySnapshotSeedPromptUsesSnapshotContract() throws Exception {
        String seed = new ClassPathResource("db/seed/R__20260601_user_memory_snapshot_prompt_seed.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(seed)
                .contains("USER_MEMORY", "SNAPSHOT_SUMMARY", "USER_MEMORY_SNAPSHOT_DEFAULT");
        assertThat(seed)
                .contains("\"summary\"", "\"recurring_themes\"", "\"important_people\"",
                        "\"stress_factors\"", "\"recovery_factors\"", "\"trait_summary\"");
        assertThat(seed)
                .contains("{{source_from_date}}", "{{source_to_date}}", "{{events_json}}",
                        "{{trait_profiles_json}}");
    }
}
