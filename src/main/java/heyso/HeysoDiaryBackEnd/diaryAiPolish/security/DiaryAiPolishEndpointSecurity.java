package heyso.HeysoDiaryBackEnd.diaryAiPolish.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.config.EndpointSecurity;

@Component
public class DiaryAiPolishEndpointSecurity implements EndpointSecurity {

    private static final String API_BASE_URL = "/api/diary-ai-polish";

    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, API_BASE_URL + "/**").authenticated();
        auth.requestMatchers(HttpMethod.GET, API_BASE_URL + "/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, API_BASE_URL + "/**").authenticated();
        auth.requestMatchers(HttpMethod.PATCH, API_BASE_URL + "/**").authenticated();
        auth.requestMatchers(HttpMethod.DELETE, API_BASE_URL + "/**").authenticated();
    }
}
