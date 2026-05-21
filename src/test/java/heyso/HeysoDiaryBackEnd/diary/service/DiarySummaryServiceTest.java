package heyso.HeysoDiaryBackEnd.diary.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import heyso.HeysoDiaryBackEnd.diary.mapper.DiarySummaryMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiarySummaryServiceTest {

    @Mock
    private DiarySummaryMapper diarySummaryMapper;

    @InjectMocks
    private DiarySummaryService diarySummaryService;

    @Test
    @DisplayName("최신 일기가 어제여도 과거 최장 연속 작성일을 집계한다")
    void rebuildSummary_calculatesLongestStreakDaysAcrossAllDiaryHistory() {
        Long userId = 5L;
        LocalDate lastDiaryDate = LocalDate.of(2026, 5, 11);

        when(diarySummaryMapper.countActiveDiaries(userId)).thenReturn(10L);
        when(diarySummaryMapper.selectLastDiaryDate(userId)).thenReturn(lastDiaryDate);
        when(diarySummaryMapper.selectDistinctDiaryDatesDesc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 9),
                        LocalDate.of(2026, 5, 8),
                        LocalDate.of(2026, 5, 7),
                        LocalDate.of(2026, 4, 23),
                        LocalDate.of(2026, 4, 22),
                        LocalDate.of(2026, 4, 19),
                        LocalDate.of(2026, 4, 17)));
        when(diarySummaryMapper.selectAllTimeTopTags(eq(userId), any(Integer.class))).thenReturn(List.of());
        when(diarySummaryMapper.selectYearlyTopTags(eq(userId), any(Integer.class))).thenReturn(List.of());

        diarySummaryService.rebuildSummary(userId);

        verify(diarySummaryMapper).upsertSummaryCache(userId, 10L, 3, lastDiaryDate);
    }
}
