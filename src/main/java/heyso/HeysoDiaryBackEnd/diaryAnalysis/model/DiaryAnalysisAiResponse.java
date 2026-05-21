package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAnalysisAiResponse {
    private String summary;
    private List<EventItem> events;

    @JsonProperty("trait_evidence")
    private List<TraitEvidenceItem> traitEvidence;

    @Getter
    @Setter
    public static class EventItem {
        @JsonProperty("event_type")
        private String eventType;
        @JsonProperty("event_title")
        private String eventTitle;
        @JsonProperty("event_summary")
        private String eventSummary;
        private String emotion;
        @JsonProperty("emotion_intensity")
        private BigDecimal emotionIntensity;
        private JsonNode people;
        private JsonNode places;
        private JsonNode activities;
        private JsonNode relationship;
        private JsonNode causality;
        @JsonProperty("pattern_candidate")
        private JsonNode patternCandidate;
        private BigDecimal confidence;
        @JsonProperty("evidence_text")
        private String evidenceText;
    }

    @Getter
    @Setter
    public static class TraitEvidenceItem {
        @JsonProperty("trait_key")
        private String traitKey;
        @JsonProperty("signal_score")
        private BigDecimal signalScore;
        private BigDecimal confidence;
        @JsonProperty("evidence_text")
        private String evidenceText;
        private JsonNode reason;
    }
}
