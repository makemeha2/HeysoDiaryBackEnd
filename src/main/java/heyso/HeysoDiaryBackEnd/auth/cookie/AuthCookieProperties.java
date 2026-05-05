package heyso.HeysoDiaryBackEnd.auth.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieProperties {
    public static final String ACCESS_TOKEN_COOKIE_NAME = "heyso_access_token";
    public static final String CSRF_TOKEN_COOKIE_NAME = "heyso_csrf_token";
    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    @Value("${app.auth.jwt-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.auth.cookie-secure:true}")
    private boolean secure;

    @Value("${app.auth.cookie-same-site:Lax}")
    private String sameSite;

    public long accessTokenMaxAgeSeconds() {
        return Math.max(1, accessTokenExpirationMs / 1000);
    }

    public boolean secure() {
        return secure;
    }

    public String sameSite() {
        return sameSite;
    }
}
