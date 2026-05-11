package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DiarySummaryCache {
    private Long userId;
    private Long totalDiaryCount;
    private Integer currentStreakDays;
    private LocalDate lastDiaryDate;
    private LocalDateTime generatedAt;
    private Boolean dirty;
}
