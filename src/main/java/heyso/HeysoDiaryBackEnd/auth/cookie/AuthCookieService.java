package heyso.HeysoDiaryBackEnd.auth.cookie;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthCookieService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthCookieProperties properties;

    public void addAuthCookies(HttpServletResponse response, String accessToken) {
        String csrfToken = generateCsrfToken();
        addCookie(response, buildAccessCookie(accessToken, properties.accessTokenMaxAgeSeconds()));
        addCookie(response, buildCsrfCookie(csrfToken, properties.accessTokenMaxAgeSeconds()));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, buildAccessCookie("", 0));
        addCookie(response, buildCsrfCookie("", 0));
    }

    public String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, AuthCookieProperties.ACCESS_TOKEN_COOKIE_NAME);
    }

    public String extractCsrfToken(HttpServletRequest request) {
        return extractCookieValue(request, AuthCookieProperties.CSRF_TOKEN_COOKIE_NAME);
    }

    private ResponseCookie buildAccessCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(AuthCookieProperties.ACCESS_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie buildCsrfCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(AuthCookieProperties.CSRF_TOKEN_COOKIE_NAME, value)
                .httpOnly(false)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String generateCsrfToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
