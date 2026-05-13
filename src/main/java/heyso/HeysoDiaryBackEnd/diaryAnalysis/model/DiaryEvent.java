package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryEvent {
    private Long analysisId;
    private Long diaryId;
    private Long userId;
    private String eventType;
    private String eventTitle;
    private String eventSummary;
    private String emotion;
    private BigDecimal emotionIntensity;
    private String peopleJson;
    private String placesJson;
    private String activitiesJson;
    private String relationshipJson;
    private String causalityJson;
    private String patternCandidateJson;
    private BigDecimal confidence;
    private String evidenceText;
}
