package heyso.HeysoDiaryBackEnd.auth.service.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.azure.communication.email.EmailClient;

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
@TestPropertySource(properties = "azure.communication.email.connection-string=")
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

    @Test
    @Timeout(30)
    void sendOtpEmail_viaEmailSender_fallsBackToLoggingWithoutRealSend() {
        assertThat(connectionString).isBlank();
        assertThat(emailClient).isNull();

        String otpCode = String.format("%04d", Math.abs(UUID.randomUUID().hashCode()) % 10_000);
        log.info("Testing OTP email fallback logging path. to={}", TEST_RECIPIENT);
        emailSender.sendEmail(
                TEST_RECIPIENT,
                "[Heyso Diary] Account Deletion OTP",
                "<p>Your OTP code is: <strong>" + otpCode + "</strong></p><p>This code expires in 10 minutes.</p>");
    }
}
