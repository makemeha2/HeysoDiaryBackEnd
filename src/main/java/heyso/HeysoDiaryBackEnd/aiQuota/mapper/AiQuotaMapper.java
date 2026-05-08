package heyso.HeysoDiaryBackEnd.aiQuota.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aiQuota.model.AiQuotaDailyUsage;
import heyso.HeysoDiaryBackEnd.aiQuota.model.AiQuotaUsageLog;

@Mapper
public interface AiQuotaMapper {

    int insertDailyUsageIfAbsent(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate);

    AiQuotaDailyUsage selectDailyUsage(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate);

    int incrementUsageIfAvailable(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate,
            @Param("effectiveLimit") Integer effectiveLimit);

    int decrementUsage(@Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate);

    Integer sumActiveGrantAmount(@Param("userId") Long userId,
            @Param("grantDate") LocalDate grantDate);

    int insertUsageLog(AiQuotaUsageLog usageLog);

    int releaseUsageLog(@Param("id") Long id);
}
