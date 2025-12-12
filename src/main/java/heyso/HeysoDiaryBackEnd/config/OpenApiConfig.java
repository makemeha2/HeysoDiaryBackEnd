package heyso.HeysoDiaryBackEnd.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String BEARER_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI heysoDiaryOpenAPI() {
                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT access token")))
                                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                                .info(new Info()
                                                .title("Heyso Diary API")
                                                .description("Diary listing and management APIs")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("Heyso Diary")
                                                                .email("support@heyso.com")));
        }
}
