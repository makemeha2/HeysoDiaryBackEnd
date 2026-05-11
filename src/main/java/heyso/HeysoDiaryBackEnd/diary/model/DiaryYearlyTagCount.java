package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryYearlyTagCount {
    private String year;
    private String tag;
    private Long tagCount;
}
