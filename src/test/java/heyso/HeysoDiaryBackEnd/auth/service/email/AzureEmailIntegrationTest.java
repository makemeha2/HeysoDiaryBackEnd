package heyso.HeysoDiaryBackEnd.auth.service.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import heyso.HeysoDiaryBackEnd.mail.config.EmailConfiguration;
import heyso.HeysoDiaryBackEnd.mail.sender.EmailSender;
import heyso.HeysoDiaryBackEnd.mail.sender.LoggingEmailSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = {
        EmailConfiguration.class,
        LoggingEmailSender.class
})
@ActiveProfiles("local")
class AzureEmailIntegrationTest {

    private static final String TEST_RECIPIENT = "makemeha2@gmail.com";

    @Autowired(required = false)
    private EmailClient emailClient;

    @Autowired
    private EmailSender emailSender;

    @Value("${azure.communication.email.sender-address}")
    private String senderAddress;

    @Value("${azure.communication.email.connection-string:}")
    private String connectionString;

    @BeforeEach
    void skipIfAzureEmailNotConfigured() {
        log.info("senderAddress={}", senderAddress);
        log.info("connectionString exists={}", StringUtils.hasText(connectionString));

        Assumptions.assumeTrue(
                StringUtils.hasText(connectionString),
                "azure.communication.email.connection-string is blank");
    }

    @Test
    @Timeout(30)
    void sendOtpEmail_viaEmailSender() {
        String otpCode = String.format("%04d", Math.abs(UUID.randomUUID().hashCode()) % 10_000);
        log.info("Sending OTP email via EmailSender to {}", TEST_RECIPIENT);
        emailSender.sendEmail(
                TEST_RECIPIENT,
                "[Heyso Diary] Account Deletion OTP",
                "<p>Your OTP code is: <strong>" + otpCode + "</strong></p><p>This code expires in 10 minutes.</p>");
    }

    @Test
    @Timeout(120)
    void sendMail_directlyViaAzureClient_andCheckCompletion() {
        Assumptions.assumeTrue(emailClient != null, "EmailClient bean is not created");

        String subject = "[Heyso Diary] Azure Email Integration Test " + System.currentTimeMillis();
        String body = "Integration test mail. OTP sample: 1234";

        EmailMessage message = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setToRecipients(TEST_RECIPIENT)
                .setSubject(subject)
                .setBodyPlainText(body);

        log.info("Begin Azure email send. from={}, to={}, subject={}", senderAddress, TEST_RECIPIENT, subject);
        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> firstPoll = poller.poll();
        if (firstPoll.getValue() != null) {
            log.info("Azure email operation id={}", firstPoll.getValue().getId());
        }

        PollResponse<EmailSendResult> response = poller.waitForCompletion(Duration.ofSeconds(90));
        log.info("Azure email final status={}", response.getStatus());
        if (response.getValue() != null) {
            log.info("Azure email final operation id={}", response.getValue().getId());
        }

        assertThat(response.getStatus()).isEqualTo(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        assertThat(response.getValue()).isNotNull();
        assertThat(response.getValue().getId()).isNotBlank();
    }
}
