package heyso.HeysoDiaryBackEnd.diaryAiPolish.service;

import java.time.LocalDate;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.diaryAiPolish.mapper.DiaryAiPolishMapper;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishDailyUsage;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishLog;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryAiPolishPersistenceService {

    private final DiaryAiPolishMapper diaryAiPolishMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createRequestLog(Long userId, Long diaryId, int requestTextLength) {
        DiaryAiPolishLog log = new DiaryAiPolishLog();
        log.setUserId(userId);
        log.setDiaryId(diaryId);
        log.setRequestTextLength(requestTextLength);
        log.setRequestStatus("REQUESTED");
        log.setUsedQuotaYn("N");
        diaryAiPolishMapper.insertPolishLog(log);
        return log.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DiaryAiPolishDailyUsage reserveUsage(Long userId, LocalDate usageDate, int quotaLimit) {
        // 제한 초과 요청도 먼저 요청 로그를 남긴 뒤 실패 처리해 운영 추적성을 유지한다.
        diaryAiPolishMapper.insertDailyUsageIfAbsent(userId, usageDate, quotaLimit);
        int updated = diaryAiPolishMapper.incrementUsageIfAvailable(userId, usageDate, quotaLimit);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily AI polish limit exceeded");
        }

        return diaryAiPolishMapper.selectDailyUsage(userId, usageDate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DiaryAiPolishResult saveSuccess(Long polishLogId,
            Long userId,
            Long diaryId,
            String originalContent,
            String polishedContent) {
        DiaryAiPolishResult result = new DiaryAiPolishResult();
        result.setPolishLogId(polishLogId);
        result.setUserId(userId);
        result.setDiaryId(diaryId);
        result.setOriginalContent(originalContent);
        result.setPolishedContent(polishedContent);
        result.setAppliedYn("N");
        result.setSavedYn("N");
        diaryAiPolishMapper.insertPolishResult(result);

        diaryAiPolishMapper.updatePolishLogSuccess(polishLogId);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long polishLogId, DiaryAiPolishFailureCode failReasonCode) {
        diaryAiPolishMapper.updatePolishLogFailure(polishLogId, failReasonCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseUsageAndMarkFailed(Long polishLogId,
            Long userId,
            LocalDate usageDate,
            DiaryAiPolishFailureCode failReasonCode) {
        diaryAiPolishMapper.decrementUsage(userId, usageDate);
        diaryAiPolishMapper.updatePolishLogFailure(polishLogId, failReasonCode);
    }

    public DiaryAiPolishFailureCode resolveFailureCode(Throwable throwable) {
        if (throwable instanceof DataAccessException) {
            return DiaryAiPolishFailureCode.DB_PROCESSING_FAILED;
        }

        if (throwable instanceof ResponseStatusException responseStatusException) {
            HttpStatus status = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            if (status == HttpStatus.TOO_MANY_REQUESTS) {
                return DiaryAiPolishFailureCode.DAILY_LIMIT_EXCEEDED;
            }
            if (status == HttpStatus.BAD_GATEWAY) {
                String reason = responseStatusException.getReason();
                if (reason != null
                        && (reason.contains("empty polished content") || reason.contains("AI content is empty"))) {
                    return DiaryAiPolishFailureCode.EMPTY_AI_RESPONSE;
                }
                return DiaryAiPolishFailureCode.AI_CALL_FAILED;
            }
        }

        return DiaryAiPolishFailureCode.INTERNAL_ERROR;
    }
}
