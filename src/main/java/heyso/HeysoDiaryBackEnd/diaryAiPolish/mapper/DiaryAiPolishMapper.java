package heyso.HeysoDiaryBackEnd.diaryAiPolish.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishDailyUsage;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishLog;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;

@Mapper
public interface DiaryAiPolishMapper {

    int insertDailyUsageIfAbsent(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate,
            @Param("quotaLimit") Integer quotaLimit);

    DiaryAiPolishDailyUsage selectDailyUsage(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate);

    int incrementUsageIfAvailable(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate,
            @Param("quotaLimit") Integer quotaLimit);

    int decrementUsage(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate);

    int insertPolishLog(DiaryAiPolishLog log);

    int updatePolishLogSuccess(@Param("id") Long id);

    int updatePolishLogFailure(@Param("id") Long id,
            @Param("failReasonCode") DiaryAiPolishFailureCode failReasonCode);

    int insertPolishResult(DiaryAiPolishResult result);
}
