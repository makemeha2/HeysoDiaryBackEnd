package heyso.HeysoDiaryBackEnd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiarySummaryApiResponse {
    private LocalDateTime generatedAt;
    private String source;
    private Stats stats;
    private TagRankings tagRankings;

    @Getter
    @AllArgsConstructor
    public static class Stats {
        private long totalDiaryCount;
        private int currentStreakDays;
    }

    @Getter
    @AllArgsConstructor
    public static class TagRankings {
        private List<TagCount> allTime;
        private List<YearlyTagRanking> yearly;
    }

    @Getter
    @AllArgsConstructor
    public static class TagCount {
        private String tag;
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class YearlyTagRanking {
        private String year;
        private List<TagCount> tags;
    }
}
