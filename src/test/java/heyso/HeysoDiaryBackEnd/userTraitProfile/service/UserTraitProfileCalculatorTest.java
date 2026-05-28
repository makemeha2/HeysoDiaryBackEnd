package heyso.HeysoDiaryBackEnd.userTraitProfile.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitEvidenceAggregate;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfile;

class UserTraitProfileCalculatorTest {
    private final UserTraitProfileCalculator calculator = new UserTraitProfileCalculator();

    @Test
    @DisplayName("confidence 가중 평균으로 long/recent score와 profile confidence를 계산한다")
    void calculate_weightedAverageScores() {
        UserTraitEvidenceAggregate aggregate = aggregate(
                "SELF_REFLECTION",
                "0.960",
                "1.600",
                2,
                "0.400",
                "0.800");

        List<UserTraitProfile> profiles = calculator.calculate(
                10L,
                List.of(aggregate),
                LocalDate.of(2026, 5, 28));

        assertThat(profiles).hasSize(1);
        UserTraitProfile profile = profiles.get(0);
        assertThat(profile.getUserId()).isEqualTo(10L);
        assertThat(profile.getTraitKey()).isEqualTo("SELF_REFLECTION");
        assertThat(profile.getLongTermScore()).isEqualByComparingTo("0.600");
        assertThat(profile.getRecentScore()).isEqualByComparingTo("0.500");
        assertThat(profile.getConfidence()).isEqualByComparingTo("0.320");
        assertThat(profile.getEvidenceCount()).isEqualTo(2);
        assertThat(profile.getCalculatedDate()).isEqualTo(LocalDate.of(2026, 5, 28));
    }

    @Test
    @DisplayName("recent 근거가 없으면 recent_score는 0으로 저장한다")
    void calculate_recentScoreZeroWhenRecentEvidenceMissing() {
        UserTraitEvidenceAggregate aggregate = aggregate(
                "GOAL_ORIENTATION",
                "-0.800",
                "1.000",
                1,
                "0.000",
                "0.000");

        List<UserTraitProfile> profiles = calculator.calculate(
                10L,
                List.of(aggregate),
                LocalDate.of(2026, 5, 28));

        assertThat(profiles.get(0).getLongTermScore()).isEqualByComparingTo("-0.800");
        assertThat(profiles.get(0).getRecentScore()).isEqualByComparingTo("0.000");
    }

    @Test
    @DisplayName("score는 -1~1로 clamp하고 profile confidence는 1을 넘지 않는다")
    void calculate_clampsScoreAndConfidence() {
        UserTraitEvidenceAggregate aggregate = aggregate(
                "PERSISTENCE",
                "7.500",
                "5.000",
                6,
                "-7.500",
                "5.000");

        List<UserTraitProfile> profiles = calculator.calculate(
                10L,
                List.of(aggregate),
                LocalDate.of(2026, 5, 28));

        assertThat(profiles.get(0).getLongTermScore()).isEqualByComparingTo("1.000");
        assertThat(profiles.get(0).getRecentScore()).isEqualByComparingTo("-1.000");
        assertThat(profiles.get(0).getConfidence()).isEqualByComparingTo("1.000");
    }

    @Test
    @DisplayName("long-term confidence 합계가 0이면 profile을 만들지 않는다")
    void calculate_skipsZeroConfidenceAggregate() {
        UserTraitEvidenceAggregate aggregate = aggregate(
                "SELF_CARE",
                "0.000",
                "0.000",
                0,
                "0.000",
                "0.000");

        List<UserTraitProfile> profiles = calculator.calculate(
                10L,
                List.of(aggregate),
                LocalDate.of(2026, 5, 28));

        assertThat(profiles).isEmpty();
    }

    private UserTraitEvidenceAggregate aggregate(
            String traitKey,
            String longTermWeightedScoreSum,
            String longTermConfidenceSum,
            int evidenceCount,
            String recentWeightedScoreSum,
            String recentConfidenceSum) {
        UserTraitEvidenceAggregate aggregate = new UserTraitEvidenceAggregate();
        aggregate.setTraitKey(traitKey);
        aggregate.setLongTermWeightedScoreSum(new BigDecimal(longTermWeightedScoreSum));
        aggregate.setLongTermConfidenceSum(new BigDecimal(longTermConfidenceSum));
        aggregate.setEvidenceCount(evidenceCount);
        aggregate.setRecentWeightedScoreSum(new BigDecimal(recentWeightedScoreSum));
        aggregate.setRecentConfidenceSum(new BigDecimal(recentConfidenceSum));
        return aggregate;
    }
}
