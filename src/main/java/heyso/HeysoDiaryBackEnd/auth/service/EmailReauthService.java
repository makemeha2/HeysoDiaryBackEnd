package heyso.HeysoDiaryBackEnd.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.dto.EmailReauthSendResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.ReauthStatusResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.ReauthVerifyResponse;
import heyso.HeysoDiaryBackEnd.auth.mapper.EmailReauthMapper;
import heyso.HeysoDiaryBackEnd.auth.model.EmailOtp;
import heyso.HeysoDiaryBackEnd.auth.model.ReauthGrant;
import heyso.HeysoDiaryBackEnd.auth.service.email.EmailSender;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailReauthService {

    private static final int OTP_LENGTH = 4;
    private static final int OTP_MAX_FAIL_COUNT = 5;
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration REAUTH_GRANT_TTL = Duration.ofMinutes(5);

    private final EmailReauthMapper emailReauthMapper;
    private final UserMapper userMapper;
    private final EmailSender emailSender;
    private final PasswordEncoder otpPasswordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public EmailReauthSendResponse sendOtpForAccountDelete(Long userId, String requestIp, String requestUa) {
        User user = requireActiveUser(userId);
        ReauthPurpose purpose = ReauthPurpose.ACCOUNT_DELETE;

        emailReauthMapper.invalidateActiveOtps(userId, purpose);
        Integer latestResendCount = emailReauthMapper.selectLatestResendCount(userId, purpose);
        int resendCount = latestResendCount == null ? 0 : latestResendCount + 1;

        String otp = generateNumericOtp(OTP_LENGTH);
        LocalDateTime expiresAt = LocalDateTime.now().plus(OTP_TTL);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUserId(userId);
        emailOtp.setPurpose(purpose);
        emailOtp.setEmail(user.getEmail());
        emailOtp.setOtpHash(otpPasswordEncoder.encode(otp));
        emailOtp.setExpiresAt(expiresAt);
        emailOtp.setSendStatus(EmailOtpSendStatus.PENDING);
        emailOtp.setFailCount(0);
        emailOtp.setResendCount(resendCount);
        emailOtp.setLastSentAt(LocalDateTime.now());
        emailOtp.setRequestIp(requestIp);
        emailOtp.setRequestUa(requestUa);
        emailReauthMapper.insertEmailOtp(emailOtp);

        try {
            emailSender.sendAccountDeleteOtp(user.getEmail(), otp);
            emailReauthMapper.updateEmailOtpSendStatus(emailOtp.getOtpId(), EmailOtpSendStatus.SENT);
        } catch (RuntimeException ex) {
            emailReauthMapper.updateEmailOtpSendStatus(emailOtp.getOtpId(), EmailOtpSendStatus.FAILED);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
        }

        return new EmailReauthSendResponse(
                purpose.name(),
                expiresAt,
                maskEmail(user.getEmail()));
    }

    @Transactional
    public ReauthVerifyResponse verifyOtpForAccountDelete(
            Long userId,
            String otp,
            String verifyIp,
            String verifyUa) {
                
        requireActiveUser(userId);
        ReauthPurpose purpose = ReauthPurpose.ACCOUNT_DELETE;
        EmailOtp latestOtp = emailReauthMapper.selectLatestOtpForVerify(userId, purpose);

        if (latestOtp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not requested");
        }
        if (latestOtp.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already consumed");
        }
        if (latestOtp.getVerifiedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already verified");
        }
        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }
        if (latestOtp.getFailCount() >= OTP_MAX_FAIL_COUNT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OTP attempts exceeded");
        }

        if (!otpPasswordEncoder.matches(otp, latestOtp.getOtpHash())) {
            emailReauthMapper.increaseEmailOtpFailCount(latestOtp.getOtpId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "OTP mismatch");
        }

        int verifiedRows = emailReauthMapper.markEmailOtpVerified(latestOtp.getOtpId(), verifyIp, verifyUa);
        if (verifiedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "OTP verification state changed");
        }

        Instant grantExpiresAtInstant = Instant.now().plus(REAUTH_GRANT_TTL);
        LocalDateTime grantExpiresAt = LocalDateTime.ofInstant(grantExpiresAtInstant, ZoneId.systemDefault());
        ReauthGrant reauthGrant = new ReauthGrant();
        reauthGrant.setUserId(userId);
        reauthGrant.setPurpose(purpose);
        reauthGrant.setGrantedByType(ReauthGrantType.EMAIL_OTP);
        reauthGrant.setSourceOtpId(latestOtp.getOtpId());
        reauthGrant.setExpiresAt(grantExpiresAt);
        emailReauthMapper.insertReauthGrant(reauthGrant);

        return new ReauthVerifyResponse(purpose.name(), grantExpiresAtInstant);
    }

    @Transactional(readOnly = true)
    public ReauthStatusResponse getReauthStatus(Long userId, ReauthPurpose purpose) {
        ReauthGrant activeGrant = emailReauthMapper.selectActiveGrant(userId, purpose);
        return new ReauthStatusResponse(
                purpose.name(),
                activeGrant != null,
                activeGrant != null ? activeGrant.getExpiresAt() : null);
    }

    @Transactional
    public ReauthGrant consumeActiveGrant(Long userId, ReauthPurpose purpose) {
        ReauthGrant activeGrant = emailReauthMapper.selectActiveGrantForUpdate(userId, purpose);
        if (activeGrant == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reauthentication is required");
        }

        int consumedRows = emailReauthMapper.consumeReauthGrant(activeGrant.getGrantId());
        if (consumedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reauthentication grant expired");
        }

        if (activeGrant.getSourceOtpId() != null) {
            emailReauthMapper.markEmailOtpConsumed(activeGrant.getSourceOtpId());
        }
        return activeGrant;
    }

    private String generateNumericOtp(int length) {
        int bound = (int) Math.pow(10, length);
        int min = (int) Math.pow(10, length - 1);
        int value = secureRandom.nextInt(bound - min) + min;
        return String.valueOf(value);
    }

    private User requireActiveUser(Long userId) {
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!Objects.equals(user.getStatus(), "ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email is unavailable");
        }
        return user;
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(at, 0));
        }
        String name = email.substring(0, at);
        String domain = email.substring(at);
        return name.charAt(0) + "***" + domain;
    }
}
