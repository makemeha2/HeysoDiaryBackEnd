package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper.DiaryAnalysisMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryAnalysisDirtyMarkerTest {
    @Mock
    private DiaryAnalysisMapper diaryAnalysisMapper;

    @Test
    @DisplayName("동일 입력은 동일한 content hash를 생성한다")
    void buildContentHash_returnsSameHashForSameInput() {
        DiaryAnalysisDirtyMarker marker = new DiaryAnalysisDirtyMarker(diaryAnalysisMapper);

        String first = marker.buildContentHash("title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));
        String second = marker.buildContentHash("title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
    }

    @Test
    @DisplayName("태그 순서만 다른 입력은 동일한 content hash를 생성한다")
    void buildContentHash_sortsTagsBeforeHashing() {
        DiaryAnalysisDirtyMarker marker = new DiaryAnalysisDirtyMarker(diaryAnalysisMapper);

        String first = marker.buildContentHash("title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));
        String second = marker.buildContentHash("title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("life", "work"));

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("분석 대상 값이 바뀌면 content hash가 변경된다")
    void buildContentHash_changesWhenContentChanges() {
        DiaryAnalysisDirtyMarker marker = new DiaryAnalysisDirtyMarker(diaryAnalysisMapper);

        String original = marker.buildContentHash("title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));
        String changed = marker.buildContentHash("title", "changed", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));

        assertThat(original).isNotEqualTo(changed);
    }

    @Test
    @DisplayName("markDirty는 생성한 content hash로 state upsert를 요청한다")
    void markDirty_callsMapperWithContentHash() {
        DiaryAnalysisDirtyMarker marker = new DiaryAnalysisDirtyMarker(diaryAnalysisMapper);

        marker.markDirty(10L, 20L, "title", "content", LocalDate.of(2026, 5, 12), "happy",
                List.of("work", "life"));

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(diaryAnalysisMapper).markDiaryAnalysisDirty(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(20L),
                hashCaptor.capture());
        assertThat(hashCaptor.getValue()).hasSize(64);
    }
}
