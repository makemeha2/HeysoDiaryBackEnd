package heyso.HeysoDiaryBackEnd.diary.mapper;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiaryMapper {
    List<DiarySummary> selectDiaryList(@Param("request") DiaryListRequest request);

    void insertDiary(Diary diary);
}
