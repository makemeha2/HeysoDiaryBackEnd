package heyso.HeysoDiaryBackEnd.diaryAiPolish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishLog;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;

@Mapper
public interface DiaryAiPolishMapper {

    int insertPolishLog(DiaryAiPolishLog log);

    int updatePolishLogSuccess(@Param("id") Long id);

    int updatePolishLogFailure(@Param("id") Long id,
            @Param("failReasonCode") DiaryAiPolishFailureCode failReasonCode);

    int insertPolishResult(DiaryAiPolishResult result);
}
