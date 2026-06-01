package heyso.HeysoDiaryBackEnd.userMemorySnapshot.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMemorySnapshotEventSource {
    private Long eventId;
    private Long diaryId;
    private LocalDate diaryDate;
    private String eventType;
    private String eventTitle;
    private String eventSummary;
    private String emotion;
    private BigDecimal emotionIntensity;
    private BigDecimal confidence;
    private String evidenceText;
    private String peopleJson;
    private String placesJson;
    private String activitiesJson;
    private String relationshipJson;
    private String causalityJson;
    private String patternCandidateJson;
    private LocalDateTime updatedAt;
}
