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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.aiQuota.dto.AiQuotaReservation;
import heyso.HeysoDiaryBackEnd.aiQuota.dto.AiQuotaStatusResponse;
import heyso.HeysoDiaryBackEnd.aiQuota.exception.AiQuotaExceededException;
import heyso.HeysoDiaryBackEnd.aiQuota.model.AiFeatureType;
import heyso.HeysoDiaryBackEnd.aiQuota.service.AiQuotaService;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishRequest;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishResponse;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.model.DiaryAiPolishResult;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.support.DiaryAiPolishAiClient;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishFailureCode;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.type.DiaryAiPolishMode;
import heyso.HeysoDiaryBackEnd.user.model.User;

@ExtendWith(MockitoExtension.class)
class DiaryAiPolishServiceTest {

    @Mock
    private DiaryMapper diaryMapper;

    @Mock
    private DiaryAiPolishAiClient diaryAiPolishAiClient;

    @Mock
    private DiaryAiPolishPersistenceService persistenceService;

    @Mock
    private AiQuotaService aiQuotaService;

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
    @DisplayName("글다듬기 요청 시 50자 미만 입력이면 실패한다")
    void requestPolish_fails_whenContentIsShorterThan50Chars() {
        DiaryAiPolishRequest request = request(10L, "a".repeat(49));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("글다듬기 요청 시 2000자를 초과하면 실패한다")
    void requestPolish_fails_whenContentExceeds2000Chars() {
        DiaryAiPolishRequest request = request(10L, "a".repeat(2001));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("글다듬기 요청 시 하루 제한을 초과하면 실패 로그만 남긴다")
    void requestPolish_fails_whenDailyLimitExceeded_andKeepsFailureLog() {
        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(101L);
        when(aiQuotaService.reserveQuota(eq(1L), any(LocalDate.class), eq(AiFeatureType.POLISH), eq(101L)))
                .thenThrow(new AiQuotaExceededException(3));

        DiaryAiPolishRequest request = request(10L, "a".repeat(60));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(AiQuotaExceededException.class);

        verify(persistenceService).markFailed(101L, DiaryAiPolishFailureCode.DAILY_LIMIT_EXCEEDED);
        verify(aiQuotaService, never()).releaseQuota(any(), any(), any());
    }

    @Test
    @DisplayName("글다듬기 요청 시 AI 호출에 실패하면 사용량을 복구한다")
    void requestPolish_releasesUsage_whenAiCallFails() {
        AiQuotaReservation quotaReservation = quotaReservation(3, 2, 201L);
        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(102L);
        when(aiQuotaService.reserveQuota(eq(1L), any(LocalDate.class), eq(AiFeatureType.POLISH), eq(102L)))
                .thenReturn(quotaReservation);
        when(diaryAiPolishAiClient.polish("a".repeat(60), DiaryAiPolishMode.STRICT))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI polish request failed"));
        when(persistenceService.resolveFailureCode(any())).thenReturn(DiaryAiPolishFailureCode.AI_CALL_FAILED);

        DiaryAiPolishRequest request = request(10L, "a".repeat(60));

        assertThatThrownBy(() -> diaryAiPolishService.requestPolish(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);

        verify(aiQuotaService).releaseQuota(eq(1L), any(LocalDate.class), eq(201L));
        verify(persistenceService).markFailed(102L, DiaryAiPolishFailureCode.AI_CALL_FAILED);
    }

    @Test
    @DisplayName("글다듬기 요청 성공 시 로그와 결과를 저장하고 남은 횟수를 반환한다")
    void requestPolish_savesLogAndResult_onSuccess_andUsesQuotaLimitForRemainingCount() {
        AiQuotaReservation quotaReservation = quotaReservation(5, 2, 202L);
        DiaryAiPolishResult result = new DiaryAiPolishResult();
        result.setOriginalContent("a".repeat(60));
        result.setPolishedContent("polished");
        result.setAppliedYn("N");

        when(diaryMapper.selectDiaryById(10L)).thenReturn(ownedDiary(10L, 1L));
        when(persistenceService.createRequestLog(1L, 10L, 60)).thenReturn(103L);
        when(aiQuotaService.reserveQuota(eq(1L), any(LocalDate.class), eq(AiFeatureType.POLISH), eq(103L)))
                .thenReturn(quotaReservation);
        when(diaryAiPolishAiClient.polish("a".repeat(60), DiaryAiPolishMode.STRICT))
                .thenReturn(new AiResponse("polished", AiProvider.OPENAI, "gpt-4o-mini", "req-1", 1, 1, 2));
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
    @DisplayName("글다듬기 요청 시 diaryId 소유자가 다르면 접근을 거부한다")
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

    private AiQuotaReservation quotaReservation(int dailyLimit, int usedCount, Long usageLogId) {
        return new AiQuotaReservation(AiQuotaStatusResponse.of(usedCount, dailyLimit), usageLogId);
    }

    private DiarySummary ownedDiary(Long diaryId, Long authorId) {
        DiarySummary diary = new DiarySummary();
        diary.setDiaryId(diaryId);
        diary.setAuthorId(authorId);
        return diary;
    }
}
