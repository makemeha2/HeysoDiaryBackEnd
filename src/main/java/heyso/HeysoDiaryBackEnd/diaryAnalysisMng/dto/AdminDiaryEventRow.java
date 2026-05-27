package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDiaryEventRow {
    private Long eventId;
    private Long analysisId;
    private Long diaryId;
    private Long userId;
    private String eventType;
    private String eventTitle;
    private String eventSummary;
    private String emotion;
    private String emotionName;
    private BigDecimal emotionIntensity;
    private String peopleJson;
    private String placesJson;
    private String activitiesJson;
    private String relationshipJson;
    private String causalityJson;
    private String patternCandidateJson;
    private BigDecimal confidence;
    private String evidenceText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
