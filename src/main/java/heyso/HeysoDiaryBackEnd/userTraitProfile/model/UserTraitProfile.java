package heyso.HeysoDiaryBackEnd.userTraitProfile.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTraitProfile {
    private Long userId;
    private String traitKey;
    private BigDecimal longTermScore;
    private BigDecimal recentScore;
    private BigDecimal confidence;
    private Integer evidenceCount;
    private LocalDate calculatedDate;
}
