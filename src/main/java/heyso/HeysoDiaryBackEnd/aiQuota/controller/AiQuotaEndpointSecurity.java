package heyso.HeysoDiaryBackEnd.aiQuota.controller;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.config.EndpointSecurity;

@Component
public class AiQuotaEndpointSecurity implements EndpointSecurity {

    private static final String API_BASE_URL = "/api/ai-quota";

    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.GET, API_BASE_URL + "/**").authenticated();
    }
}
