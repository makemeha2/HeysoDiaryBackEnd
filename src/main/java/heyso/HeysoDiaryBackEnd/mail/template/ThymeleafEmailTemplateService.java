package heyso.HeysoDiaryBackEnd.mail.template;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private static final String ACCOUNT_DELETE_OTP_SUBJECT = "[Heyso Diary] 회원탈퇴 인증 코드 안내";
    private static final String WELCOME_SUBJECT = "[Heyso Diary] 가입을 환영합니다";
    private static final String ACCOUNT_DELETE_OTP_HTML_TEMPLATE = "mail/account-delete-otp";
    private static final String WELCOME_TEMPLATE = "mail/welcome";

    private final SpringTemplateEngine htmlTemplateEngine;

    public ThymeleafEmailTemplateService(
            @Qualifier("mailHtmlTemplateEngine") SpringTemplateEngine htmlTemplateEngine) {
        this.htmlTemplateEngine = htmlTemplateEngine;
    }

    @Override
    public EmailTemplate createAccountDeleteOtpTemplate(String otpCode, Duration expiresIn) {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiresInMinutes", expiresIn.toMinutes());

        String htmlBody = htmlTemplateEngine.process(ACCOUNT_DELETE_OTP_HTML_TEMPLATE, context);
        return new EmailTemplate(ACCOUNT_DELETE_OTP_SUBJECT, htmlBody);

    }

    @Override
    public EmailTemplate createWelcomeTemplate(String nickname) {
        Context context = new Context();
        context.setVariable("nickname", StringUtils.hasText(nickname) ? nickname : "회원");

        String htmlBody = htmlTemplateEngine.process(WELCOME_TEMPLATE, context);
        return new EmailTemplate(WELCOME_SUBJECT, htmlBody);
    }
}
