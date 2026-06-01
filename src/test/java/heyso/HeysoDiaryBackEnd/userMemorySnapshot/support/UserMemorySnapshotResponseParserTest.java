package heyso.HeysoDiaryBackEnd.userMemorySnapshot.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotParsedResponse;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.type.UserMemorySnapshotException;

class UserMemorySnapshotResponseParserTest {
    private final UserMemorySnapshotResponseParser parser = new UserMemorySnapshotResponseParser(new ObjectMapper());

    @Test
    @DisplayName("정상 JSON을 snapshot 저장 필드로 변환한다")
    void parseValidResponse() {
        UserMemorySnapshotParsedResponse result = parser.parse("""
                {
                  "summary": "최근 관계와 회복 루틴이 중요한 사용자다.",
                  "recurring_themes": [{"theme": "관계"}],
                  "important_people": [{"name": "친구"}],
                  "stress_factors": [{"factor": "업무"}],
                  "recovery_factors": [{"factor": "산책"}],
                  "trait_summary": [{"trait_key": "SELF_REFLECTION"}]
                }
                """);

        assertThat(result.summaryText()).isEqualTo("최근 관계와 회복 루틴이 중요한 사용자다.");
        assertThat(result.recurringThemesJson()).isEqualTo("[{\"theme\":\"관계\"}]");
        assertThat(result.importantPeopleJson()).isEqualTo("[{\"name\":\"친구\"}]");
        assertThat(result.traitSummaryJson()).isEqualTo("[{\"trait_key\":\"SELF_REFLECTION\"}]");
    }

    @Test
    @DisplayName("code fence로 감싼 JSON도 파싱한다")
    void parseCodeFenceResponse() {
        UserMemorySnapshotParsedResponse result = parser.parse("""
                ```json
                {
                  "summary": "요약",
                  "recurring_themes": [],
                  "important_people": [],
                  "stress_factors": [],
                  "recovery_factors": [],
                  "trait_summary": {}
                }
                ```
                """);

        assertThat(result.summaryText()).isEqualTo("요약");
        assertThat(result.traitSummaryJson()).isEqualTo("{}");
    }

    @Test
    @DisplayName("summary가 없거나 blank면 실패한다")
    void rejectBlankSummary() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "summary": " ",
                  "recurring_themes": [],
                  "important_people": [],
                  "stress_factors": [],
                  "recovery_factors": [],
                  "trait_summary": []
                }
                """))
                .isInstanceOf(UserMemorySnapshotException.class)
                .hasMessageContaining("summary");
    }

    @Test
    @DisplayName("JSON container가 아닌 필드는 스키마 실패로 처리한다")
    void rejectScalarContainerField() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "summary": "요약",
                  "recurring_themes": "관계",
                  "important_people": [],
                  "stress_factors": [],
                  "recovery_factors": [],
                  "trait_summary": []
                }
                """))
                .isInstanceOf(UserMemorySnapshotException.class)
                .hasMessageContaining("recurring_themes");
    }
}
