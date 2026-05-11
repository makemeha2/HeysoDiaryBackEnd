package heyso.HeysoDiaryBackEnd.diary.mapper;

import heyso.HeysoDiaryBackEnd.diary.model.DiarySummaryCache;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTagCount;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTagSummaryCache;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryYearlyTagCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DiarySummaryMapper {
    DiarySummaryCache selectSummaryCache(@Param("userId") Long userId);

    List<DiaryTagSummaryCache> selectTagSummaryCache(@Param("userId") Long userId);

    long countActiveDiaries(@Param("userId") Long userId);

    LocalDate selectLastDiaryDate(@Param("userId") Long userId);

    List<LocalDate> selectDistinctDiaryDatesDesc(@Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate);

    List<DiaryTagCount> selectAllTimeTopTags(@Param("userId") Long userId,
            @Param("limit") int limit);

    List<DiaryYearlyTagCount> selectYearlyTopTags(@Param("userId") Long userId,
            @Param("limit") int limit);

    void upsertSummaryCache(@Param("userId") Long userId,
            @Param("totalDiaryCount") long totalDiaryCount,
            @Param("currentStreakDays") int currentStreakDays,
            @Param("lastDiaryDate") LocalDate lastDiaryDate);

    void deleteTagSummaryCache(@Param("userId") Long userId);

    void insertTagSummaryCaches(@Param("items") List<DiaryTagSummaryCache> items);

    void markSummaryDirty(@Param("userId") Long userId);

    List<Long> selectDirtyUserIds();
}
