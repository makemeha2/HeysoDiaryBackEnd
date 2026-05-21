package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.config.EndpointSecurity;

@Component
@Order(0)
public class DiaryAnalysisMngEndpointSecurity implements EndpointSecurity {

    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/admin/diary-analysis/**").hasAuthority("SCOPE_admin");
    }
}
