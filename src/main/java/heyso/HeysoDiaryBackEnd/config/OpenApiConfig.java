package heyso.HeysoDiaryBackEnd.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI heysoDiaryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Heyso Diary API")
                        .description("Diary listing and management APIs")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Heyso Diary")
                                .email("support@heyso.com")));
    }
}
