package heyso.HeysoDiaryBackEnd.userMemorySnapshot.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMemorySnapshot {
    private Long snapshotId;
    private Long userId;
    private Integer snapshotVersion;
    private String summaryText;
    private String recurringThemesJson;
    private String importantPeopleJson;
    private String stressFactorsJson;
    private String recoveryFactorsJson;
    private String traitSummaryJson;
    private LocalDate sourceFromDate;
    private LocalDate sourceToDate;
    private String sourceJson;
}
