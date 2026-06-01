package heyso.HeysoDiaryBackEnd.userTraitProfile.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitEvidenceAggregate;
import heyso.HeysoDiaryBackEnd.userTraitProfile.model.UserTraitProfile;

@Mapper
public interface UserTraitProfileMapper {
    List<Long> selectChangedUserIds();

    List<UserTraitEvidenceAggregate> selectTraitEvidenceAggregates(
            @Param("userId") Long userId,
            @Param("confidenceThreshold") BigDecimal confidenceThreshold,
            @Param("recentFromDate") LocalDate recentFromDate);

    void upsertUserTraitProfiles(@Param("profiles") List<UserTraitProfile> profiles);

    int deactivateActiveProfilesExceptTraitKeys(
            @Param("userId") Long userId,
            @Param("traitKeys") List<String> traitKeys,
            @Param("calculatedDate") LocalDate calculatedDate);

    int deactivateAllActiveProfiles(
            @Param("userId") Long userId,
            @Param("calculatedDate") LocalDate calculatedDate);
}
