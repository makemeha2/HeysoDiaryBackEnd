package heyso.HeysoDiaryBackEnd.aiQuota.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.aiQuota.dto.AiQuotaReservation;
import heyso.HeysoDiaryBackEnd.aiQuota.dto.AiQuotaStatusResponse;
import heyso.HeysoDiaryBackEnd.aiQuota.exception.AiQuotaExceededException;
import heyso.HeysoDiaryBackEnd.aiQuota.mapper.AiQuotaMapper;
import heyso.HeysoDiaryBackEnd.aiQuota.model.AiFeatureType;
import heyso.HeysoDiaryBackEnd.aiQuota.model.AiQuotaDailyUsage;
import heyso.HeysoDiaryBackEnd.aiQuota.model.AiQuotaUsageLog;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiQuotaService {

    private static final int BASE_DAILY_LIMIT = 2;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final AiQuotaMapper aiQuotaMapper;

    public AiQuotaStatusResponse getStatus(Long userId) {
        LocalDate today = LocalDate.now(KOREA_ZONE);
        int effectiveLimit = computeEffectiveLimit(userId, today);
        AiQuotaDailyUsage usage = aiQuotaMapper.selectDailyUsage(userId, today);
        int usedCount = usage == null || usage.getUsedCount() == null ? 0 : usage.getUsedCount();
        return AiQuotaStatusResponse.of(usedCount, effectiveLimit);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiQuotaReservation reserveQuota(Long userId,
            LocalDate usageDate,
            AiFeatureType featureType,
            Long featureRefId) {
        aiQuotaMapper.insertDailyUsageIfAbsent(userId, usageDate);

        int effectiveLimit = computeEffectiveLimit(userId, usageDate);
        int updated = aiQuotaMapper.incrementUsageIfAvailable(userId, usageDate, effectiveLimit);
        if (updated == 0) {
            throw new AiQuotaExceededException(effectiveLimit);
        }

        AiQuotaUsageLog usageLog = new AiQuotaUsageLog();
        usageLog.setUserId(userId);
        usageLog.setUsageDate(usageDate);
        usageLog.setFeatureType(featureType);
        usageLog.setFeatureRefId(featureRefId);
        usageLog.setStatus("SUCCESS");
        aiQuotaMapper.insertUsageLog(usageLog);

        AiQuotaDailyUsage usage = aiQuotaMapper.selectDailyUsage(userId, usageDate);
        int usedCount = usage == null || usage.getUsedCount() == null ? 0 : usage.getUsedCount();
        return new AiQuotaReservation(AiQuotaStatusResponse.of(usedCount, effectiveLimit), usageLog.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseQuota(Long userId, LocalDate usageDate, Long usageLogId) {
        if (userId == null || usageDate == null || usageLogId == null) {
            return;
        }
        aiQuotaMapper.decrementUsage(userId, usageDate);
        aiQuotaMapper.releaseUsageLog(usageLogId);
    }

    private int computeEffectiveLimit(Long userId, LocalDate date) {
        Integer grantAmount = aiQuotaMapper.sumActiveGrantAmount(userId, date);
        return BASE_DAILY_LIMIT + (grantAmount == null ? 0 : grantAmount);
    }
}
