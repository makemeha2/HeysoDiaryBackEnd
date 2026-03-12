package heyso.HeysoDiaryBackEnd.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.auth.model.EmailOtp;
import heyso.HeysoDiaryBackEnd.auth.model.ReauthGrant;
import heyso.HeysoDiaryBackEnd.auth.service.ReauthPurpose;

@Mapper
public interface EmailReauthMapper {

    int invalidateActiveOtps(@Param("userId") Long userId, @Param("purpose") ReauthPurpose purpose);

    Integer selectLatestResendCount(@Param("userId") Long userId, @Param("purpose") ReauthPurpose purpose);

    int insertEmailOtp(EmailOtp emailOtp);

    int updateEmailOtpSendStatus(@Param("otpId") Long otpId, @Param("sendStatus") String sendStatus);

    EmailOtp selectLatestOtpForVerify(@Param("userId") Long userId, @Param("purpose") ReauthPurpose purpose);

    int increaseEmailOtpFailCount(@Param("otpId") Long otpId);

    int markEmailOtpVerified(
            @Param("otpId") Long otpId,
            @Param("verifyIp") String verifyIp,
            @Param("verifyUa") String verifyUa);

    int markEmailOtpConsumed(@Param("otpId") Long otpId);

    int insertReauthGrant(ReauthGrant reauthGrant);

    ReauthGrant selectActiveGrant(@Param("userId") Long userId, @Param("purpose") ReauthPurpose purpose);

    ReauthGrant selectActiveGrantForUpdate(@Param("userId") Long userId, @Param("purpose") ReauthPurpose purpose);

    int consumeReauthGrant(@Param("grantId") Long grantId);
}
