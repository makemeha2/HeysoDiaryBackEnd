package heyso.HeysoDiaryBackEnd.auth.service.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.azure.communication.email.EmailClient;

import heyso.HeysoDiaryBackEnd.mail.config.EmailConfiguration;
import heyso.HeysoDiaryBackEnd.mail.sender.LoggingEmailSender;

@SpringBootTest(classes = {
        EmailConfiguration.class,
        LoggingEmailSender.class
})
@ActiveProfiles("local")
@TestPropertySource(properties = "azure.communication.email.connection-string=")
class AzureEmailPropertyBindingTest {

    @Value("${azure.communication.email.connection-string:}")
    private String connectionString;

    @Value("${azure.communication.email.sender-address:}")
    private String senderAddress;

    @Autowired(required = false)
    private EmailClient emailClient;

    @Test
    void localAzureEmailProperties_areLoaded_withoutCreatingRealClientInTests() {
        assertThat(senderAddress).isEqualTo("DoNotReply@heyso-diary.com");
        assertThat(connectionString).isBlank();
        assertThat(emailClient).isNull();
    }
}
