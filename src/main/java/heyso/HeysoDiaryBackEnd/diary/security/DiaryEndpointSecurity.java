package heyso.HeysoDiaryBackEnd.diary.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.config.EndpointSecurity;

@Component
public class DiaryEndpointSecurity implements EndpointSecurity {
    private final String apiBaseUrl = "/api/diary";

    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, apiBaseUrl + "/**").authenticated();
        auth.requestMatchers(HttpMethod.POST, apiBaseUrl + "/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, apiBaseUrl + "/**").authenticated();
        auth.requestMatchers(HttpMethod.PATCH, apiBaseUrl + "/**").authenticated();
        auth.requestMatchers(HttpMethod.DELETE, apiBaseUrl + "/**").authenticated();
    }
}
