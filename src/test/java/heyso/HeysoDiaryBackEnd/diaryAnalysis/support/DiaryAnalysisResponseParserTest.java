package heyso.HeysoDiaryBackEnd.diaryAnalysis.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisResult;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisErrorCode;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisException;

class DiaryAnalysisResponseParserTest {
    private final DiaryAnalysisResponseParser parser = new DiaryAnalysisResponseParser(new ObjectMapper());

    @Test
    @DisplayName("정상 구조화 JSON을 event/evidence 저장 모델로 변환한다")
    void parseValidResponse() {
        DiaryAnalysisResult result = parser.parse("""
                {
                  "summary": "친구와 대화하며 마음을 정리한 하루",
                  "events": [
                    {
                      "event_type": "RELATIONSHIP",
                      "event_title": "친구와 대화",
                      "event_summary": "친구와 고민을 나누었다.",
                      "emotion": "CALM",
                      "emotion_intensity": 0.7,
                      "people": ["친구"],
                      "places": [],
                      "activities": ["대화"],
                      "relationship": {"type": "friend"},
                      "causality": {},
                      "pattern_candidate": {},
                      "confidence": 0.8,
                      "evidence_text": "친구와 이야기를 했다"
                    }
                  ],
                  "trait_evidence": [
                    {
                      "trait_key": "SELF_REFLECTION",
                      "signal_score": 0.6,
                      "confidence": 0.75,
                      "evidence_text": "내 감정을 돌아봤다",
                      "reason": {"basis": "reflection"}
                    }
                  ]
                }
                """, candidate(), Set.of("RELATIONSHIP"), Set.of("CALM"), Set.of("SELF_REFLECTION"));

        assertThat(result.summary()).isEqualTo("친구와 대화하며 마음을 정리한 하루");
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0).getDiaryId()).isEqualTo(10L);
        assertThat(result.events().get(0).getPeopleJson()).isEqualTo("[\"친구\"]");
        assertThat(result.traitEvidence()).hasSize(1);
        assertThat(result.traitEvidence().get(0).getTraitKey()).isEqualTo("SELF_REFLECTION");
        assertThat(result.rawResponseJson()).contains("\"summary\"");
    }

    @Test
    @DisplayName("허용되지 않은 trait_key는 스키마 실패로 처리한다")
    void rejectUnknownTraitKey() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "summary": "요약",
                  "events": [],
                  "trait_evidence": [
                    {
                      "trait_key": "UNKNOWN_TRAIT",
                      "signal_score": 0.2,
                      "confidence": 0.5,
                      "evidence_text": "근거",
                      "reason": {}
                    }
                  ]
                }
                """, candidate(), Set.of("RELATIONSHIP"), Set.of("CALM"), Set.of("SELF_REFLECTION")))
                .isInstanceOf(DiaryAnalysisException.class)
                .extracting("errorCode")
                .isEqualTo(DiaryAnalysisErrorCode.SCHEMA_INVALID);
    }

    @Test
    @DisplayName("JSON 파싱 실패는 JSON_PARSE_FAILED로 처리한다")
    void rejectInvalidJson() {
        assertThatThrownBy(() -> parser.parse("not-json", candidate(),
                Set.of("RELATIONSHIP"), Set.of("CALM"), Set.of("SELF_REFLECTION")))
                .isInstanceOf(DiaryAnalysisException.class)
                .extracting("errorCode")
                .isEqualTo(DiaryAnalysisErrorCode.JSON_PARSE_FAILED);
    }

    private DiaryAnalysisCandidate candidate() {
        DiaryAnalysisCandidate candidate = new DiaryAnalysisCandidate();
        candidate.setDiaryId(10L);
        candidate.setUserId(20L);
        candidate.setTitle("제목");
        candidate.setContentMd("본문");
        candidate.setDiaryDate(LocalDate.of(2026, 5, 13));
        candidate.setMoodId("CALM");
        candidate.setDiaryUpdatedAt(LocalDateTime.of(2026, 5, 13, 12, 0));
        candidate.setContentHash("hash");
        return candidate;
    }
}
