package heyso.HeysoDiaryBackEnd.aiQuota.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiQuotaStatusResponse {

    private Integer usedCount;
    private Integer dailyLimit;
    private Integer remainingCount;

    public static AiQuotaStatusResponse of(Integer usedCount, Integer dailyLimit) {
        int safeUsedCount = usedCount == null ? 0 : usedCount;
        int safeDailyLimit = dailyLimit == null ? 0 : dailyLimit;
        return new AiQuotaStatusResponse(
                safeUsedCount,
                safeDailyLimit,
                Math.max(0, safeDailyLimit - safeUsedCount));
    }
}
