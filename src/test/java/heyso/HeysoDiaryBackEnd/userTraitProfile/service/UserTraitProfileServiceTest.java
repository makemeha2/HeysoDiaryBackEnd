package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.userTraitProfile.mapper.UserTraitProfileMapper;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfileRebuildResult;

@ExtendWith(MockitoExtension.class)
class UserTraitProfileServiceTest {
    @Mock
    private UserTraitProfileMapper userTraitProfileMapper;

    @Mock
    private UserTraitProfilePersistenceService persistenceService;

    @InjectMocks
    private UserTraitProfileService service;

    @Test
    @DisplayName("변경 사용자가 없으면 persistence 호출 없이 종료한다")
    void rebuildChangedUserProfiles_returnsEmptyResultWhenNoChangedUsers() {
        when(userTraitProfileMapper.selectChangedUserIds()).thenReturn(List.of());

        AdminBatchRunResult result = service.rebuildChangedUserProfiles();

        assertThat(result.successCount()).isZero();
        assertThat(result.failureCount()).isZero();
        assertThat(result.message()).contains("집계 대상 사용자가 없습니다");
        verifyNoInteractions(persistenceService);
    }

    @Test
    @DisplayName("사용자 단위 실패가 발생해도 다음 사용자를 계속 처리한다")
    void rebuildChangedUserProfiles_continuesAfterUserFailure() {
        when(userTraitProfileMapper.selectChangedUserIds()).thenReturn(List.of(10L, 20L));
        when(persistenceService.rebuildUserProfile(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new IllegalStateException("boom"));
        when(persistenceService.rebuildUserProfile(eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new UserTraitProfileRebuildResult(2, 1));

        AdminBatchRunResult result = service.rebuildChangedUserProfiles();

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.message()).contains("처리 대상 2명", "성공 1명", "실패 1명", "갱신 profile 2건", "비활성화 profile 1건");
    }

    @Test
    @DisplayName("recent_score 기준일은 calculatedDate 포함 최근 30일로 전달한다")
    void rebuildChangedUserProfiles_usesThirtyDayRecentWindow() {
        when(userTraitProfileMapper.selectChangedUserIds()).thenReturn(List.of(10L));
        when(persistenceService.rebuildUserProfile(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new UserTraitProfileRebuildResult(1, 0));

        service.rebuildChangedUserProfiles();

        ArgumentCaptor<LocalDate> calculatedDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> recentFromDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        org.mockito.Mockito.verify(persistenceService).rebuildUserProfile(
                eq(10L),
                calculatedDateCaptor.capture(),
                recentFromDateCaptor.capture());
        assertThat(recentFromDateCaptor.getValue()).isEqualTo(calculatedDateCaptor.getValue().minusDays(29));
    }
}
