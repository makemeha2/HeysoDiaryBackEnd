package heyso.HeysoDiaryBackEnd.auth.service.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import com.azure.communication.email.EmailClient;

import heyso.HeysoDiaryBackEnd.mail.config.EmailConfiguration;
import heyso.HeysoDiaryBackEnd.mail.sender.LoggingEmailSender;

@SpringBootTest(classes = {
        EmailConfiguration.class,
        LoggingEmailSender.class
})
@ActiveProfiles("local")
class AzureEmailPropertyBindingTest {

    @Value("${azure.communication.email.connection-string:}")
    private String connectionString;

    @Value("${azure.communication.email.sender-address:}")
    private String senderAddress;

    @Autowired(required = false)
    private EmailClient emailClient;

    @Test
    void localAzureEmailProperties_areLoaded_andClientBeanIsCreated() {
        assertThat(senderAddress).isEqualTo("DoNotReply@heyso-diary.com");
        if (StringUtils.hasText(connectionString)) {
            assertThat(connectionString).contains("endpoint=https://heysocommservices.korea.communication.azure.com/");
            assertThat(connectionString).contains("accesskey=");
            assertThat(emailClient).isNotNull();
            return;
        }
        assertThat(emailClient).isNull();
    }
}
