package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DiaryTagSummaryCache {
    private Long userId;
    private String periodType;
    private String periodKey;
    private String tag;
    private Long tagCount;
    private Integer rankNo;
    private LocalDateTime generatedAt;
}
