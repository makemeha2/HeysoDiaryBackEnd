package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraitDefinition {
    private String traitKey;
    private String traitCategory;
    private String traitName;
    private String traitDescription;
    private String scoreDirection;
    private Integer sortSeq;
}
