package heyso.HeysoDiaryBackEnd.diaryAiPolish.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallResult;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishRequest;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishResponse;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishDailyUsage;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.support.DiaryAiPolishAiClient;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryAiPolishService {

    private static final int DAILY_LIMIT = 3;
    private static final int MIN_CONTENT_LENGTH = 50;
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final DiaryMapper diaryMapper;
    private final DiaryAiPolishAiClient diaryAiPolishAiClient;
    private final DiaryAiPolishPersistenceService persistenceService;

    public DiaryAiPolishResponse requestPolish(DiaryAiPolishRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Long diaryId = request.getDiaryId();
        String content = normalizeContent(request.getContent());
        validateContent(content);
        validateDiaryAccessIfPresent(user.getUserId(), diaryId);

        LocalDate usageDate = LocalDate.now(KOREA_ZONE);
        Long polishLogId = persistenceService.createRequestLog(user.getUserId(), diaryId, content.length());

        boolean usageReserved = false;

        try {
            DiaryAiPolishDailyUsage usage = persistenceService.reserveUsage(user.getUserId(), usageDate, DAILY_LIMIT);
            usageReserved = true;

            AiCallResult aiCallResult = diaryAiPolishAiClient.polish(content);
            DiaryAiPolishResult result = persistenceService.saveSuccess(
                    polishLogId,
                    user.getUserId(),
                    diaryId,
                    content,
                    aiCallResult.content());

            int quotaLimit = usage.getQuotaLimit() == null ? DAILY_LIMIT : usage.getQuotaLimit();
            int usedCount = usage.getUsedCount() == null ? 0 : usage.getUsedCount();
            int remainingCount = Math.max(0, quotaLimit - usedCount);

            return DiaryAiPolishResponse.of(
                    polishLogId,
                    result.getOriginalContent(),
                    result.getPolishedContent(),
                    remainingCount,
                    "Y".equals(result.getAppliedYn()),
                    "POLISHED");
        } catch (ResponseStatusException e) {
            logAiFailureIfApplicable(user.getUserId(), diaryId, polishLogId, e);
            handleFailure(polishLogId, user.getUserId(), usageDate, usageReserved, e);
            throw e;
        } catch (DataAccessException e) {
            log.error("Diary AI polish DB processing failed. userId={}, diaryId={}, logId={}, message={}",
                    user.getUserId(), diaryId, polishLogId, e.getMessage(), e);
            handleFailure(polishLogId, user.getUserId(), usageDate, usageReserved, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Diary AI polish DB processing failed");
        } catch (Exception e) {
            log.error("Unexpected diary AI polish failure. userId={}, diaryId={}, logId={}",
                    user.getUserId(), diaryId, polishLogId, e);
            handleFailure(polishLogId, user.getUserId(), usageDate, usageReserved, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Diary AI polish request failed");
        }
    }

    private void validateDiaryAccessIfPresent(Long userId, Long diaryId) {
        if (diaryId == null) {
            return;
        }

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!userId.equals(diary.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this diary");
        }
    }

    private void validateContent(String content) {
        if (StringUtils.isBlank(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must not be empty");
        }

        int contentLength = content.length();
        if (contentLength < MIN_CONTENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must be at least 50 characters");
        }
        if (contentLength > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must be 2000 characters or less");
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }
        return content.replace("\r\n", "\n");
    }

    private void handleFailure(Long polishLogId,
            Long userId,
            LocalDate usageDate,
            boolean usageReserved,
            Throwable throwable) {
        DiaryAiPolishFailureCode failureCode = persistenceService.resolveFailureCode(throwable);

        try {
            if (usageReserved && !isDailyLimitExceeded(throwable)) {
                persistenceService.releaseUsageAndMarkFailed(polishLogId, userId, usageDate, failureCode);
            } else {
                persistenceService.markFailed(polishLogId, failureCode);
            }
        } catch (Exception logUpdateException) {
            log.error("Failed to finalize diary AI polish failure. userId={}, logId={}", userId, polishLogId,
                    logUpdateException);
        }
    }

    private boolean isDailyLimitExceeded(Throwable throwable) {
        if (!(throwable instanceof ResponseStatusException responseStatusException)) {
            return false;
        }
        return HttpStatus.TOO_MANY_REQUESTS.equals(HttpStatus.resolve(responseStatusException.getStatusCode().value()));
    }

    private void logAiFailureIfApplicable(Long userId, Long diaryId, Long polishLogId, ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status != HttpStatus.BAD_GATEWAY) {
            return;
        }

        String message = exception.getReason();
        if (StringUtils.isBlank(message) && exception.getCause() != null) {
            message = exception.getCause().getMessage();
        }

        log.warn("Diary AI polish AI call failed. userId={}, diaryId={}, logId={}, message={}",
                userId, diaryId, polishLogId, message);
    }
}
