package heyso.HeysoDiaryBackEnd.mail.template;

import java.time.Duration;

public interface EmailTemplateService {

    EmailTemplate createAccountDeleteOtpTemplate(String otpCode, Duration expiresIn);

    EmailTemplate createWelcomeTemplate(String nickname);
}
