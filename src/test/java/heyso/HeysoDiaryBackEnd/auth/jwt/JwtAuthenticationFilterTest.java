package heyso.HeysoDiaryBackEnd.auth.jwt;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import heyso.HeysoDiaryBackEnd.auth.cookie.AuthCookieService;
import heyso.HeysoDiaryBackEnd.auth.service.AuthTokenService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

    private final AuthTokenService authTokenService = org.mockito.Mockito.mock(AuthTokenService.class);
    private final AuthCookieService authCookieService = org.mockito.Mockito.mock(AuthCookieService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authTokenService, authCookieService);

    @Test
    void skipsGoogleLoginEndpointEvenWhenStaleBearerTokenExists() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/oauth/google");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(authTokenService, never()).authenticate("stale-token");
    }

    @Test
    void skipsGoogleLoginEndpointWithContextPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/heysodiary/api/auth/oauth/google");
        request.setContextPath("/heysodiary");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(authTokenService, never()).authenticate("stale-token");
    }

    @Test
    void stillAuthenticatesProtectedApiRequests() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/diary");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authTokenService.authenticate("stale-token"))
                .thenThrow(new JwtAuthException(JwtAuthError.INACTIVE, "User is not active"));

        filter.doFilter(request, response, new MockFilterChain());

        verify(authTokenService).authenticate("stale-token");
    }
}
