package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.userTraitProfile.mapper.UserTraitProfileMapper;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitEvidenceAggregate;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfile;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfileRebuildResult;

@ExtendWith(MockitoExtension.class)
class UserTraitProfilePersistenceServiceTest {
    @Mock
    private UserTraitProfileMapper userTraitProfileMapper;

    @Mock
    private UserTraitProfileCalculator userTraitProfileCalculator;

    @InjectMocks
    private UserTraitProfilePersistenceService service;

    @Test
    @DisplayName("eligible evidence 집계 후 profile upsert와 누락 trait 비활성화를 수행한다")
    void rebuildUserProfile_upsertsProfilesAndDeactivatesMissingTraits() {
        Long userId = 10L;
        LocalDate calculatedDate = LocalDate.of(2026, 5, 28);
        LocalDate recentFromDate = LocalDate.of(2026, 4, 29);
        UserTraitEvidenceAggregate aggregate = new UserTraitEvidenceAggregate();
        UserTraitProfile profile = profile(userId, "SELF_REFLECTION", calculatedDate);

        when(userTraitProfileMapper.selectTraitEvidenceAggregates(
                eq(userId),
                any(BigDecimal.class),
                eq(recentFromDate)))
                .thenReturn(List.of(aggregate));
        when(userTraitProfileCalculator.calculate(userId, List.of(aggregate), calculatedDate))
                .thenReturn(List.of(profile));
        when(userTraitProfileMapper.deactivateActiveProfilesExceptTraitKeys(
                eq(userId),
                eq(List.of("SELF_REFLECTION")),
                eq(calculatedDate)))
                .thenReturn(2);

        UserTraitProfileRebuildResult result = service.rebuildUserProfile(userId, calculatedDate, recentFromDate);

        ArgumentCaptor<BigDecimal> thresholdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userTraitProfileMapper).selectTraitEvidenceAggregates(
                eq(userId),
                thresholdCaptor.capture(),
                eq(recentFromDate));
        assertThat(thresholdCaptor.getValue()).isEqualByComparingTo("0.700");
        verify(userTraitProfileMapper).upsertUserTraitProfiles(List.of(profile));
        assertThat(result.upsertedProfileCount()).isEqualTo(1);
        assertThat(result.deactivatedProfileCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("집계 결과가 없으면 upsert 없이 기존 active profile 전체를 비활성화한다")
    void rebuildUserProfile_deactivatesAllProfilesWhenNoEvidenceRemains() {
        Long userId = 10L;
        LocalDate calculatedDate = LocalDate.of(2026, 5, 28);
        LocalDate recentFromDate = LocalDate.of(2026, 4, 29);

        when(userTraitProfileMapper.selectTraitEvidenceAggregates(
                eq(userId),
                any(BigDecimal.class),
                eq(recentFromDate)))
                .thenReturn(List.of());
        when(userTraitProfileCalculator.calculate(userId, List.of(), calculatedDate))
                .thenReturn(List.of());
        when(userTraitProfileMapper.deactivateAllActiveProfiles(userId, calculatedDate))
                .thenReturn(3);

        UserTraitProfileRebuildResult result = service.rebuildUserProfile(userId, calculatedDate, recentFromDate);

        verify(userTraitProfileMapper).deactivateAllActiveProfiles(userId, calculatedDate);
        verify(userTraitProfileMapper, never()).upsertUserTraitProfiles(any());
        assertThat(result.upsertedProfileCount()).isZero();
        assertThat(result.deactivatedProfileCount()).isEqualTo(3);
    }

    private UserTraitProfile profile(Long userId, String traitKey, LocalDate calculatedDate) {
        UserTraitProfile profile = new UserTraitProfile();
        profile.setUserId(userId);
        profile.setTraitKey(traitKey);
        profile.setLongTermScore(new BigDecimal("0.600"));
        profile.setRecentScore(new BigDecimal("0.500"));
        profile.setConfidence(new BigDecimal("0.320"));
        profile.setEvidenceCount(2);
        profile.setCalculatedDate(calculatedDate);
        return profile;
    }
}
