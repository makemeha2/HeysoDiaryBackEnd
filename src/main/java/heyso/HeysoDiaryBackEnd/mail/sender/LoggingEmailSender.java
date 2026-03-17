package heyso.HeysoDiaryBackEnd.mail.sender;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    private final Optional<EmailClient> emailClient;
    private final String senderAddress;

    public LoggingEmailSender(
            Optional<EmailClient> emailClient,
            @Value("${azure.communication.email.sender-address:}") String senderAddress) {
        this.emailClient = emailClient;
        this.senderAddress = senderAddress;
    }

    @Override
    public void sendEmail(String toEmail, String subject, String htmlBody) {
        if (emailClient.isEmpty() || !StringUtils.hasText(senderAddress)) {
            log.info(
                    "EmailSender fallback logging only. to={}, subject={}, htmlLength={}",
                    maskEmail(toEmail),
                    subject,
                    safeLength(htmlBody));
            return;
        }

        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(toEmail)
                    .setSubject(subject);

            if (StringUtils.hasText(htmlBody)) {
                message.setBodyHtml(htmlBody);
            }

            emailClient.get().beginSend(message);
            log.info("Email sent via Azure Communication Service. to={}, subject={}", maskEmail(toEmail), subject);
        } catch (Exception ex) {
            log.error(
                    "Email send failed. Falling back to logging only. to={}, subject={}",
                    maskEmail(toEmail),
                    subject,
                    ex);
        }
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(at, 0));
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
