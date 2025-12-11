package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryMonthlyCount {

    private LocalDate diaryDate;
    private long diaryCount;
}
