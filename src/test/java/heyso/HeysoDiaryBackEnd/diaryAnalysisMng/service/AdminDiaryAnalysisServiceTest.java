package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.service.DiaryAnalysisDirtyMarker;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.mapper.AdminDiaryAnalysisMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;

@ExtendWith(MockitoExtension.class)
class AdminDiaryAnalysisServiceTest {
    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @Mock
    private AdminDiaryAnalysisMapper adminDiaryAnalysisMapper;

    @Mock
    private DiaryMapper diaryMapper;

    @Mock
    private DiaryAnalysisDirtyMarker diaryAnalysisDirtyMarker;

    @Test
    @DisplayName("관리자 재분석 요청은 dirty 처리 후 배치 eligible 상태로 보정한다")
    void requestReanalysis_marksDirtyAndEligible() {
        AdminDiaryAnalysisService service = new AdminDiaryAnalysisService(
                adminAuthorizationService,
                adminDiaryAnalysisMapper,
                diaryMapper,
                diaryAnalysisDirtyMarker);
        DiarySummary diary = new DiarySummary();
        diary.setDiaryId(10L);
        diary.setAuthorId(20L);
        diary.setTitle("title");
        diary.setContentMd("content");
        diary.setDiaryDate(LocalDate.of(2026, 5, 21));
        diary.setMoodId("happy");
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        when(adminAuthorizationService.requireAdminUser()).thenReturn(new User());
        when(diaryMapper.selectDiaryById(10L)).thenReturn(diary);
        when(diaryMapper.selectTagNamesByDiaryId(10L)).thenReturn(List.of("work"));

        var response = service.requestReanalysis(10L);

        InOrder inOrder = inOrder(diaryAnalysisDirtyMarker, adminDiaryAnalysisMapper);
        inOrder.verify(diaryAnalysisDirtyMarker).markDirty(
                10L,
                20L,
                "title",
                "content",
                LocalDate.of(2026, 5, 21),
                "happy",
                List.of("work"));
        inOrder.verify(adminDiaryAnalysisMapper).markReanalysisEligible(10L);
        assertThat(response.getDiaryId()).isEqualTo(10L);
        assertThat(response.isDirty()).isTrue();
        assertThat(response.getAnalysisStatus()).isEqualTo("DIRTY");
    }
}
