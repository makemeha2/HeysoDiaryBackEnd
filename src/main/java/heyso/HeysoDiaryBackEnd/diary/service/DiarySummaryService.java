package heyso.HeysoDiaryBackEnd.diary.service;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.dto.DiarySummaryApiResponse;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiarySummaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummaryCache;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTagCount;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTagSummaryCache;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryYearlyTagCount;
import heyso.HeysoDiaryBackEnd.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiarySummaryService {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final int TAG_RANKING_LIMIT = 10;
    private static final String SOURCE_CACHE = "cache";
    private static final String SOURCE_REBUILT = "rebuilt";

    private final DiarySummaryMapper diarySummaryMapper;

    public DiarySummaryService(DiarySummaryMapper diarySummaryMapper) {
        this.diarySummaryMapper = diarySummaryMapper;
    }

    @Transactional(readOnly = true)
    public DiarySummaryApiResponse getMySummary() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        return getSummary(user.getUserId());
    }

    @Transactional
    public DiarySummaryApiResponse getSummary(Long userId) {
        DiarySummaryCache cache = diarySummaryMapper.selectSummaryCache(userId);
        if (cache == null || Boolean.TRUE.equals(cache.getDirty())) {
            rebuildSummary(userId);
            cache = diarySummaryMapper.selectSummaryCache(userId);
            return toResponse(cache, diarySummaryMapper.selectTagSummaryCache(userId), SOURCE_REBUILT);
        }
        return toResponse(cache, diarySummaryMapper.selectTagSummaryCache(userId), SOURCE_CACHE);
    }

    @Transactional
    public void rebuildSummary(Long userId) {
        long totalDiaryCount = diarySummaryMapper.countActiveDiaries(userId);
        LocalDate lastDiaryDate = diarySummaryMapper.selectLastDiaryDate(userId);
        int currentStreakDays = calculateCurrentStreakDays(userId);

        diarySummaryMapper.upsertSummaryCache(userId, totalDiaryCount, currentStreakDays, lastDiaryDate);
        diarySummaryMapper.deleteTagSummaryCache(userId);

        List<DiaryTagSummaryCache> tagCacheItems = buildTagCacheItems(userId);
        if (!tagCacheItems.isEmpty()) {
            diarySummaryMapper.insertTagSummaryCaches(tagCacheItems);
        }
    }

    @Transactional
    public void markSummaryDirty(Long userId) {
        diarySummaryMapper.markSummaryDirty(userId);
    }

    @Transactional(readOnly = true)
    public List<Long> getDirtyUserIds() {
        return diarySummaryMapper.selectDirtyUserIds();
    }

    private int calculateCurrentStreakDays(Long userId) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        List<LocalDate> dates = diarySummaryMapper.selectDistinctDiaryDatesDesc(userId, today);
        if (dates == null || dates.isEmpty()) {
            return 0;
        }

        LocalDate expected;
        if (dates.get(0).equals(today)) {
            expected = today;
        } else if (dates.get(0).equals(today.minusDays(1))) {
            expected = today.minusDays(1);
        } else {
            return 0;
        }

        int streak = 0;
        for (LocalDate diaryDate : dates) {
            if (!diaryDate.equals(expected)) {
                break;
            }
            streak++;
            expected = expected.minusDays(1);
        }
        return streak;
    }

    private List<DiaryTagSummaryCache> buildTagCacheItems(Long userId) {
        List<DiaryTagSummaryCache> items = new ArrayList<>();

        List<DiaryTagCount> allTimeTags = diarySummaryMapper.selectAllTimeTopTags(userId, TAG_RANKING_LIMIT);
        for (int i = 0; i < allTimeTags.size(); i++) {
            DiaryTagCount tag = allTimeTags.get(i);
            items.add(newTagCacheItem(userId, "ALL", "ALL", tag.getTag(), tag.getTagCount(), i + 1));
        }

        Map<String, Integer> yearlyRankByYear = new LinkedHashMap<>();
        List<DiaryYearlyTagCount> yearlyTags = diarySummaryMapper.selectYearlyTopTags(userId, TAG_RANKING_LIMIT);
        for (DiaryYearlyTagCount tag : yearlyTags) {
            int rankNo = yearlyRankByYear.merge(tag.getYear(), 1, Integer::sum);
            items.add(newTagCacheItem(userId, "YEAR", tag.getYear(), tag.getTag(), tag.getTagCount(), rankNo));
        }

        return items;
    }

    private DiaryTagSummaryCache newTagCacheItem(Long userId, String periodType, String periodKey,
            String tag, Long tagCount, Integer rankNo) {
        DiaryTagSummaryCache item = new DiaryTagSummaryCache();
        item.setUserId(userId);
        item.setPeriodType(periodType);
        item.setPeriodKey(periodKey);
        item.setTag(tag);
        item.setTagCount(tagCount == null ? 0L : tagCount);
        item.setRankNo(rankNo);
        return item;
    }

    private DiarySummaryApiResponse toResponse(DiarySummaryCache cache,
            List<DiaryTagSummaryCache> tagCaches,
            String source) {
        List<DiarySummaryApiResponse.TagCount> allTime = tagCaches.stream()
                .filter(tag -> "ALL".equals(tag.getPeriodType()))
                .map(this::toTagCount)
                .collect(Collectors.toList());

        Map<String, List<DiarySummaryApiResponse.TagCount>> yearlyMap = new LinkedHashMap<>();
        tagCaches.stream()
                .filter(tag -> "YEAR".equals(tag.getPeriodType()))
                .forEach(tag -> yearlyMap.computeIfAbsent(tag.getPeriodKey(), key -> new ArrayList<>())
                        .add(toTagCount(tag)));

        List<DiarySummaryApiResponse.YearlyTagRanking> yearly = yearlyMap.entrySet().stream()
                .map(entry -> new DiarySummaryApiResponse.YearlyTagRanking(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new DiarySummaryApiResponse(
                cache.getGeneratedAt(),
                source,
                new DiarySummaryApiResponse.Stats(
                        cache.getTotalDiaryCount() == null ? 0 : cache.getTotalDiaryCount(),
                        cache.getCurrentStreakDays() == null ? 0 : cache.getCurrentStreakDays()),
                new DiarySummaryApiResponse.TagRankings(allTime, yearly));
    }

    private DiarySummaryApiResponse.TagCount toTagCount(DiaryTagSummaryCache tagCache) {
        return new DiarySummaryApiResponse.TagCount(
                tagCache.getTag(),
                tagCache.getTagCount() == null ? 0 : tagCache.getTagCount());
    }
}
