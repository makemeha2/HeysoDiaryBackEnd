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
    @JsonProperty("sum")
    private String summary;
    @JsonProperty("evts")
    private List<EventItem> events;

    @JsonProperty("trait_edc")
    private List<TraitEvidenceItem> traitEvidence;

    @Getter
    @Setter
    public static class EventItem {
        @JsonProperty("evt_type")
        private String eventType;
        @JsonProperty("evt_title")
        private String eventTitle;
        @JsonProperty("evt_sum")
        private String eventSummary;
        @JsonProperty("emo")
        private String emotion;
        @JsonProperty("emo_int")
        private BigDecimal emotionIntensity;
        private JsonNode people;
        private JsonNode places;
        @JsonProperty("acts")
        private JsonNode activities;
        @JsonProperty("rel")
        private JsonNode relationship;
        @JsonProperty("cause")
        private JsonNode causality;
        @JsonProperty("pattern")
        private JsonNode patternCandidate;
        @JsonProperty("conf")
        private BigDecimal confidence;
        @JsonProperty("edc_txt")
        private String evidenceText;
    }

    @Getter
    @Setter
    public static class TraitEvidenceItem {
        @JsonProperty("trait_key")
        private String traitKey;
        @JsonProperty("sig_score")
        private BigDecimal signalScore;
        @JsonProperty("conf")
        private BigDecimal confidence;
        @JsonProperty("edc_txt")
        private String evidenceText;
        private JsonNode reason;
    }
}
