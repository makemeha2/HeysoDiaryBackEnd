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
                  "sum": "친구와 대화하며 마음을 정리한 하루",
                  "evts": [
                    {
                      "evt_type": "RELATIONSHIP",
                      "evt_title": "친구와 대화",
                      "evt_sum": "친구와 고민을 나누었다.",
                      "emo": "CALM",
                      "emo_int": 0.7,
                      "people": ["친구"],
                      "places": [],
                      "acts": ["대화"],
                      "rel": {"type": "friend"},
                      "cause": {},
                      "pattern": {},
                      "conf": 0.8,
                      "edc_txt": "친구와 이야기를 했다"
                    }
                  ],
                  "trait_edc": [
                    {
                      "trait_key": "SELF_REFLECTION",
                      "sig_score": 0.6,
                      "conf": 0.75,
                      "edc_txt": "내 감정을 돌아봤다",
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
        assertThat(result.rawResponseJson()).contains("\"sum\"");
    }

    @Test
    @DisplayName("허용되지 않은 trait_key는 스키마 실패로 처리한다")
    void rejectUnknownTraitKey() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "sum": "요약",
                  "evts": [],
                  "trait_edc": [
                    {
                      "trait_key": "UNKNOWN_TRAIT",
                      "sig_score": 0.2,
                      "conf": 0.5,
                      "edc_txt": "근거",
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
    @DisplayName("허용되지 않은 evt_type은 스키마 실패로 처리한다")
    void rejectUnknownEventType() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "sum": "요약",
                  "evts": [
                    {
                      "evt_type": "UNKNOWN_EVENT",
                      "evt_title": "사건",
                      "evt_sum": "요약",
                      "emo": null,
                      "emo_int": 0.0,
                      "conf": 0.5,
                      "edc_txt": "근거"
                    }
                  ],
                  "trait_edc": []
                }
                """, candidate(), Set.of("RELATIONSHIP"), Set.of("CALM"), Set.of("SELF_REFLECTION")))
                .isInstanceOf(DiaryAnalysisException.class)
                .extracting("errorCode")
                .isEqualTo(DiaryAnalysisErrorCode.SCHEMA_INVALID);
    }

    @Test
    @DisplayName("허용되지 않은 emo는 스키마 실패로 처리한다")
    void rejectUnknownEmotion() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "sum": "요약",
                  "evts": [
                    {
                      "evt_type": "RELATIONSHIP",
                      "evt_title": "사건",
                      "evt_sum": "요약",
                      "emo": "UNKNOWN_EMOTION",
                      "emo_int": 0.0,
                      "conf": 0.5,
                      "edc_txt": "근거"
                    }
                  ],
                  "trait_edc": []
                }
                """, candidate(), Set.of("RELATIONSHIP"), Set.of("CALM"), Set.of("SELF_REFLECTION")))
                .isInstanceOf(DiaryAnalysisException.class)
                .extracting("errorCode")
                .isEqualTo(DiaryAnalysisErrorCode.SCHEMA_INVALID);
    }

    @Test
    @DisplayName("약어 숫자 필드가 허용 범위를 벗어나면 스키마 실패로 처리한다")
    void rejectOutOfRangeCompactScores() {
        assertThatThrownBy(() -> parser.parse("""
                {
                  "sum": "요약",
                  "evts": [
                    {
                      "evt_type": "RELATIONSHIP",
                      "evt_title": "사건",
                      "evt_sum": "요약",
                      "emo": "CALM",
                      "emo_int": 1.2,
                      "conf": 0.5,
                      "edc_txt": "근거"
                    }
                  ],
                  "trait_edc": [
                    {
                      "trait_key": "SELF_REFLECTION",
                      "sig_score": 1.2,
                      "conf": 0.5,
                      "edc_txt": "근거",
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
