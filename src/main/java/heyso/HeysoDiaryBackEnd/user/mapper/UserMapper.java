package heyso.HeysoDiaryBackEnd.user.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;

@Mapper
public interface UserMapper {

    User selectUserById(@Param("userId") Long userId);

    User selectUserByEmail(@Param("email") String email);

    void insertUser(User user); // useGeneratedKeys

    UserAuth selectUserAuthByProviderAndProviderUserId(
            @Param("authProvider") String authProvider,
            @Param("providerUserId") String providerUserId);

    UserAuth selectUserAuthByLoginIdAndProvider(
            @Param("loginId") String loginId,
            @Param("authProvider") String authProvider);

    UserAuth selectUserAuthByUserIdAndProvider(
            @Param("userId") Long userId,
            @Param("authProvider") String authProvider);

    void insertUserAuth(UserAuth userAuth);

    void updateUserAuthLastLoginAt(@Param("userAuthId") Long userAuthId);

    int withdrawUser(
            @Param("userId") Long userId,
            @Param("anonymizedEmail") String anonymizedEmail,
            @Param("withdrawReasonCd") String withdrawReasonCd,
            @Param("withdrawReasonText") String withdrawReasonText);

    int deleteUserAuthByUserId(@Param("userId") Long userId);

    int updateTokenRevokedAfter(
            @Param("userId") Long userId,
            @Param("tokenRevokedAfter") LocalDateTime tokenRevokedAfter);
}
