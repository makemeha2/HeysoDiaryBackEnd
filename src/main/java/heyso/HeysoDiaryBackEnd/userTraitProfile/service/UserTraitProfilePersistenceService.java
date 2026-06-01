package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.userTraitProfile.mapper.UserTraitProfileMapper;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitEvidenceAggregate;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfile;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfileRebuildResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTraitProfilePersistenceService {
    private static final BigDecimal CONFIDENCE_THRESHOLD = new BigDecimal("0.700");

    private final UserTraitProfileMapper userTraitProfileMapper;
    private final UserTraitProfileCalculator userTraitProfileCalculator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserTraitProfileRebuildResult rebuildUserProfile(
            Long userId,
            LocalDate calculatedDate,
            LocalDate recentFromDate) {
        List<UserTraitEvidenceAggregate> aggregates = userTraitProfileMapper.selectTraitEvidenceAggregates(
                userId,
                CONFIDENCE_THRESHOLD,
                recentFromDate);
        List<UserTraitProfile> profiles = userTraitProfileCalculator.calculate(userId, aggregates, calculatedDate);

        if (!profiles.isEmpty()) {
            userTraitProfileMapper.upsertUserTraitProfiles(profiles);
        }

        int deactivatedProfileCount = deactivateMissingProfiles(userId, profiles, calculatedDate);
        return new UserTraitProfileRebuildResult(profiles.size(), deactivatedProfileCount);
    }

    private int deactivateMissingProfiles(Long userId, List<UserTraitProfile> profiles, LocalDate calculatedDate) {
        if (profiles.isEmpty()) {
            return userTraitProfileMapper.deactivateAllActiveProfiles(userId, calculatedDate);
        }

        List<String> activeTraitKeys = profiles.stream()
                .map(UserTraitProfile::getTraitKey)
                .toList();
        return userTraitProfileMapper.deactivateActiveProfilesExceptTraitKeys(
                userId,
                activeTraitKeys,
                calculatedDate);
    }
}
