package heyso.HeysoDiaryBackEnd.userMemorySnapshot.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMemorySnapshotProfileSource {
    private Long profileId;
    private String traitKey;
    private String traitCategory;
    private String traitName;
    private BigDecimal longTermScore;
    private BigDecimal recentScore;
    private BigDecimal confidence;
    private Integer evidenceCount;
    private LocalDate calculatedDate;
    private String summaryText;
    private LocalDateTime updatedAt;
}
