package heyso.HeysoDiaryBackEnd.auth.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;

@Configuration
public class EmailConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "azure.communication.email", name = "connection-string")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${azure.communication.email.connection-string:}')")
    public EmailClient azureEmailClient(
            @Value("${azure.communication.email.connection-string:}") String connectionString) {
        return new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
