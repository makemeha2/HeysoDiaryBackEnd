package heyso.HeysoDiaryBackEnd.auth.service.email;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendAccountDeleteOtp(String toEmail, String otpCode) {
        log.info("Send ACCOUNT_DELETE OTP email. to={}, otp={}", toEmail, otpCode);
    }
}
