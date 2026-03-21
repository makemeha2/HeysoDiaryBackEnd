package heyso.HeysoDiaryBackEnd.diaryAiPolish.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiPolishDailyUsage {

    private Long id;
    private Long userId;
    private LocalDate usageDate;
    private Integer quotaLimit;
    private Integer usedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
