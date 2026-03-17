package heyso.HeysoDiaryBackEnd.mail.template;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private static final String ACCOUNT_DELETE_OTP_SUBJECT = "[Heyso Diary] 회원탈퇴 인증 코드 안내";
    private static final String WELCOME_SUBJECT = "[Heyso Diary] 가입을 환영합니다";

    private final SpringTemplateEngine htmlTemplateEngine;
    @Qualifier("mailTextTemplateEngine")
    private final TemplateEngine textTemplateEngine;

    @Override
    public EmailTemplate createAccountDeleteOtpTemplate(String otpCode, Duration expiresIn) {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiresInMinutes", expiresIn.toMinutes());

        String htmlBody = htmlTemplateEngine.process("mail/account-delete-otp", context);
        String textBody = textTemplateEngine.process("mail/account-delete-otp", context);
        return new EmailTemplate(ACCOUNT_DELETE_OTP_SUBJECT, htmlBody, textBody);
    }

    @Override
    public EmailTemplate createWelcomeTemplate(String nickname) {
        Context context = new Context();
        context.setVariable("nickname", StringUtils.hasText(nickname) ? nickname : "회원");

        String htmlBody = htmlTemplateEngine.process("mail/welcome", context);
        String textBody = textTemplateEngine.process("mail/welcome", context);
        return new EmailTemplate(WELCOME_SUBJECT, htmlBody, textBody);
    }
}
