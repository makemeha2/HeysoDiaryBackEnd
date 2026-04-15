package heyso.HeysoDiaryBackEnd.monitoringMng.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.config.EndpointSecurity;

@Component
@Order(0)
public class MonitoringMngEndpointSecurity implements EndpointSecurity {

    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/admin/monitoring-events/**").hasAuthority("SCOPE_admin");
    }
}
