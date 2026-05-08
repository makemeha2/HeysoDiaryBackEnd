package heyso.HeysoDiaryBackEnd.aiQuota.exception;

import lombok.Getter;

@Getter
public class AiQuotaExceededException extends RuntimeException {

    private final int dailyLimit;

    public AiQuotaExceededException(int dailyLimit) {
        super("오늘의 AI 사용 횟수를 모두 사용했습니다.");
        this.dailyLimit = dailyLimit;
    }
}
