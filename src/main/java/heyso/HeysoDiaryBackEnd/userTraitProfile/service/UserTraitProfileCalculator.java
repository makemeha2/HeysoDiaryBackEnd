package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitEvidenceAggregate;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfile;

@Component
public class UserTraitProfileCalculator {
    private static final BigDecimal MIN_SCORE = new BigDecimal("-1.000");
    private static final BigDecimal MAX_SCORE = new BigDecimal("1.000");
    private static final BigDecimal CONFIDENCE_DENOMINATOR = new BigDecimal("5.000");
    private static final int SCALE = 3;

    public List<UserTraitProfile> calculate(Long userId,
            List<UserTraitEvidenceAggregate> aggregates,
            LocalDate calculatedDate) {
        if (aggregates == null || aggregates.isEmpty()) {
            return List.of();
        }

        return aggregates.stream()
                .filter(aggregate -> aggregate.getLongTermConfidenceSum() != null
                        && aggregate.getLongTermConfidenceSum().compareTo(BigDecimal.ZERO) > 0)
                .map(aggregate -> toProfile(userId, aggregate, calculatedDate))
                .toList();
    }

    private UserTraitProfile toProfile(Long userId,
            UserTraitEvidenceAggregate aggregate,
            LocalDate calculatedDate) {
        UserTraitProfile profile = new UserTraitProfile();
        profile.setUserId(userId);
        profile.setTraitKey(aggregate.getTraitKey());
        profile.setLongTermScore(weightedAverage(
                aggregate.getLongTermWeightedScoreSum(),
                aggregate.getLongTermConfidenceSum()));
        profile.setRecentScore(weightedAverageOrZero(
                aggregate.getRecentWeightedScoreSum(),
                aggregate.getRecentConfidenceSum()));
        profile.setConfidence(profileConfidence(aggregate.getLongTermConfidenceSum()));
        profile.setEvidenceCount(aggregate.getEvidenceCount() == null ? 0 : aggregate.getEvidenceCount());
        profile.setCalculatedDate(calculatedDate);
        return profile;
    }

    private BigDecimal weightedAverage(BigDecimal weightedScoreSum, BigDecimal confidenceSum) {
        BigDecimal score = nullToZero(weightedScoreSum)
                .divide(confidenceSum, SCALE, RoundingMode.HALF_UP);
        return clampScore(score).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal weightedAverageOrZero(BigDecimal weightedScoreSum, BigDecimal confidenceSum) {
        if (confidenceSum == null || confidenceSum.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return weightedAverage(weightedScoreSum, confidenceSum);
    }

    private BigDecimal profileConfidence(BigDecimal confidenceSum) {
        BigDecimal confidence = nullToZero(confidenceSum)
                .divide(CONFIDENCE_DENOMINATOR, SCALE, RoundingMode.HALF_UP);
        if (confidence.compareTo(BigDecimal.ONE) > 0) {
            confidence = BigDecimal.ONE;
        }
        return confidence.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal clampScore(BigDecimal score) {
        if (score.compareTo(MIN_SCORE) < 0) {
            return MIN_SCORE;
        }
        if (score.compareTo(MAX_SCORE) > 0) {
            return MAX_SCORE;
        }
        return score;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
