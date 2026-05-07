package heyso.HeysoDiaryBackEnd.auth.csrf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import heyso.HeysoDiaryBackEnd.auth.cookie.AuthCookieProperties;
import heyso.HeysoDiaryBackEnd.auth.cookie.AuthCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CookieCsrfFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.TRACE.name());

    private final AuthCookieService authCookieService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (requiresCsrfCheck(request) && !hasValidCsrfToken(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":403,\"message\":\"Invalid CSRF token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrfCheck(HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/")) {
            return false;
        }
        if ("/api/auth/oauth/google".equals(uri)
                || "/api/auth/validate".equals(uri)
                || "/api/admin/auth/login".equals(uri)) {
            return false;
        }
        return StringUtils.hasText(authCookieService.extractAccessToken(request));
    }

    private boolean hasValidCsrfToken(HttpServletRequest request) {
        String cookieToken = authCookieService.extractCsrfToken(request);
        String headerToken = request.getHeader(AuthCookieProperties.CSRF_HEADER_NAME);
        return StringUtils.hasText(cookieToken)
                && StringUtils.hasText(headerToken)
                && cookieToken.equals(headerToken);
    }
}
