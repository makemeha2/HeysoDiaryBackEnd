package heyso.HeysoDiaryBackEnd.diary.mapper;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryMonthlyCount;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiaryMapper {
    List<DiarySummary> selectDiaryList(@Param("request") DiaryListRequest request);

    List<DiaryMonthlyCount> selectDiaryMonthlyCounts(@Param("userId") Long userId,
                                                     @Param("diaryMonth") String diaryMonth);

    void insertDiary(Diary diary);

    /* ------------------------------ 태그 관련 ----------------------------------- */

    Long selectTagIdByName(@Param("tagName") String tagName);

    void insertTag(@Param("tagName") String tagName);

    void insertDiaryTag(@Param("diaryId") Long diaryId, @Param("tagId") Long tagId);

    List<DiaryTag> selectDiaryTags(@Param("diaryIds") List<Long> diaryIds);
}
