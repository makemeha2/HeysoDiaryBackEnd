package heyso.HeysoDiaryBackEnd.diaryAiPolish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallResult;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishRequest;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishResponse;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishDailyUsage;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.support.DiaryAiPolishAiClient;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;
import heyso.HeysoDiaryBackEnd.user.model.User;

@ExtendWith(MockitoExtension.class)
class DiaryAiPolishServiceTest {

    @Mock
    private DiaryMapper diaryMapper;

    @Mock
    private DiaryAiPolishAiClient diaryAiPolishAiClient;

    @Mock
    private DiaryAiPolishPersistenceService persistenceService;

    @InjectMocks
    private DiaryAiPolishService diaryAiPolishService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUserId(1L);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestPolish_fails_whenContentIsShorterThan50Chars() {
        DiaryAiPolishRequest request = request(10L, "a".repeat(49));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void requestPolish_fails_whenContentExceeds2000Chars() {
        DiaryAiPolishRequest request = request(10L, "a".repeat(2001));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void requestPolish_fails_whenDailyLimitExceeded_andKeepsFailureLog() {
        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(101L);
        when(persistenceService.reserveUsage(eq(1L), any(LocalDate.class), eq(3)))
                .thenThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily AI polish limit exceeded"));
        when(persistenceService.resolveFailureCode(any())).thenReturn(DiaryAiPolishFailureCode.DAILY_LIMIT_EXCEEDED);

        DiaryAiPolishRequest request = request(10L, "a".repeat(60));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        verify(persistenceService).markFailed(101L, DiaryAiPolishFailureCode.DAILY_LIMIT_EXCEEDED);
        verify(persistenceService, never()).releaseUsageAndMarkFailed(any(), any(), any(), any());
    }

    @Test
    void requestPolish_releasesUsage_whenAiCallFails() {
        DiaryAiPolishDailyUsage usage = usage(3, 1);
        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(102L);
        when(persistenceService.reserveUsage(eq(1L), any(LocalDate.class), eq(3))).thenReturn(usage);
        when(diaryAiPolishAiClient.polish("a".repeat(60)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI polish request failed"));
        when(persistenceService.resolveFailureCode(any())).thenReturn(DiaryAiPolishFailureCode.AI_CALL_FAILED);

        DiaryAiPolishRequest request = request(10L, "a".repeat(60));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);

        verify(persistenceService).releaseUsageAndMarkFailed(eq(102L), eq(1L), any(LocalDate.class),
                eq(DiaryAiPolishFailureCode.AI_CALL_FAILED));
    }

    @Test
    void requestPolish_savesLogAndResult_onSuccess_andUsesQuotaLimitForRemainingCount() {
        DiaryAiPolishDailyUsage usage = usage(5, 2);
        DiaryAiPolishResult result = new DiaryAiPolishResult();
        result.setOriginalContent("a".repeat(60));
        result.setPolishedContent("polished");
        result.setAppliedYn("N");

        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(103L);
        when(persistenceService.reserveUsage(eq(1L), any(LocalDate.class), eq(3))).thenReturn(usage);
        when(diaryAiPolishAiClient.polish("a".repeat(60)))
                .thenReturn(new AiCallResult("polished", "req-1", 1, 1, 2));
        when(persistenceService.saveSuccess(103L, 1L, 10L, "a".repeat(60), "polished")).thenReturn(result);

        DiaryAiPolishResponse response = diaryAiPolishService.requestPolish(request(10L, "a".repeat(60)));

        assertThat(response.getPolishLogId()).isEqualTo(103L);
        assertThat(response.getPolishedContent()).isEqualTo("polished");
        assertThat(response.getRemainingCount()).isEqualTo(3);
        assertThat(response.isApplied()).isFalse();
        assertThat(response.getStatus()).isEqualTo("POLISHED");

        verify(persistenceService).createRequestLog(1L, 10L, 60);
        verify(persistenceService).saveSuccess(103L, 1L, 10L, "a".repeat(60), "polished");
    }

    @Test
    void requestPolish_forbidsAccess_whenDiaryOwnerDoesNotMatch() {
        DiarySummary diary = new DiarySummary();
        diary.setDiaryId(10L);
        diary.setAuthorId(2L);
        when(diaryMapper.selectDiaryById(10L)).thenReturn(diary);

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request(10L, "a".repeat(60))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(responseStatusException.getReason()).isEqualTo("You cannot access this diary");
                });
    }

    private DiaryAiPolishRequest request(Long diaryId, String content) {
        DiaryAiPolishRequest request = new DiaryAiPolishRequest();
        request.setDiaryId(diaryId);
        request.setContent(content);
        return request;
    }

    private DiaryAiPolishDailyUsage usage(int quotaLimit, int usedCount) {
        DiaryAiPolishDailyUsage usage = new DiaryAiPolishDailyUsage();
        usage.setQuotaLimit(quotaLimit);
        usage.setUsedCount(usedCount);
        return usage;
    }

    private DiarySummary ownedDiary(Long diaryId, Long authorId) {
        DiarySummary diary = new DiarySummary();
        diary.setDiaryId(diaryId);
        diary.setAuthorId(authorId);
        return diary;
    }
}
