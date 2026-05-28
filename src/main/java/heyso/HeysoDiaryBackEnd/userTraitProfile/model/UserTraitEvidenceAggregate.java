package heyso.HeysoDiaryBackEnd.userTraitProfile.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTraitEvidenceAggregate {
    private String traitKey;
    private BigDecimal longTermWeightedScoreSum;
    private BigDecimal longTermConfidenceSum;
    private Integer evidenceCount;
    private BigDecimal recentWeightedScoreSum;
    private BigDecimal recentConfidenceSum;
}
