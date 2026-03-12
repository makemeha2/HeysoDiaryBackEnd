package heyso.HeysoDiaryBackEnd.auth.service.email;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public void sendEmail(String toEmail, String subject, String body) {
        if (emailClient.isEmpty() || senderAddress == null || senderAddress.isBlank()) {
            log.info(
                    "EmailSender disabled - logging only. to={}, subject={}, body={}",
                    toEmail,
                    subject,
                    body);
            return;
        }

        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(toEmail)
                    .setSubject(subject)
                    .setBodyPlainText(body);
            emailClient.get().beginSend(message);
            log.info("Email sent via Azure Communication Service. to={}, subject={}", toEmail, subject);
        } catch (Exception ex) {
            log.error("Email send failed. Falling back to logging only. to={}, subject={}", toEmail, subject, ex);
        }
    }
}
