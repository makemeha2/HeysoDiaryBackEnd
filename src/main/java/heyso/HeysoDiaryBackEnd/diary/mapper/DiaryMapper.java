package heyso.HeysoDiaryBackEnd.diary.mapper;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryMonthlyCount;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DiaryMapper {
    List<DiarySummary> selectDiaryList(@Param("request") DiaryListRequest request);

    List<DiarySummary> selectDailyDiaryList(@Param("userId") Long userId,
            @Param("diaryDate") String diaryDate);

    List<DiaryMonthlyCount> selectDiaryMonthlyCounts(@Param("userId") Long userId,
            @Param("diaryMonth") String diaryMonth);

    DiarySummary selectDiaryById(@Param("diaryId") Long diaryId);

    void insertDiary(Diary diary);

    int updateDiary(@Param("diaryId") Long diaryId,
            @Param("title") String title,
            @Param("contentMd") String contentMd,
            @Param("diaryDate") LocalDate diaryDate);

    void deleteDiary(@Param("diaryId") Long diaryId);

    void deleteDiaryTags(@Param("diaryId") Long diaryId);

    /* ------------------------------ 태그 관련 ----------------------------------- */

    Long selectTagIdByName(@Param("tagName") String tagName);

    void insertTag(@Param("tagName") String tagName);

    void insertDiaryTag(@Param("diaryId") Long diaryId, @Param("tagId") Long tagId);

    List<String> selectTagNamesByDiaryId(@Param("diaryId") Long diaryId);

    List<DiaryTag> selectDiaryTagsByDiaryId(@Param("diaryId") Long diaryId);

    List<DiaryTag> selectDiaryTags(@Param("diaryIds") List<Long> diaryIds);

    List<String> selectTagNamesByUserId(@Param("userId") Long userId);

    /* --------------------------- AI 컨텍스트 조회용 ---------------------------- */

    List<DiarySummary> selectRecentDiaries(@Param("userId") Long userId,
            @Param("excludeDiaryId") Long excludeDiaryId,
            @Param("limit") Integer limit);

    List<DiarySummary> selectDiariesByTags(@Param("userId") Long userId,
            @Param("excludeDiaryId") Long excludeDiaryId,
            @Param("tagNames") List<String> tagNames,
            @Param("limit") Integer limit);
}
