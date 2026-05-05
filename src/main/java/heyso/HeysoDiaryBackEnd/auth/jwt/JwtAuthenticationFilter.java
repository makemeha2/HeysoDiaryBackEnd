package heyso.HeysoDiaryBackEnd.auth.jwt;

import heyso.HeysoDiaryBackEnd.auth.service.AuthTokenService;
import heyso.HeysoDiaryBackEnd.auth.service.AuthenticatedToken;
import heyso.HeysoDiaryBackEnd.user.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                AuthenticatedToken authenticatedToken = authTokenService.authenticate(token);
                User user = authenticatedToken.getUser();

                List<GrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(authenticatedToken.getRole())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + authenticatedToken.getRole()));
                }
                if (StringUtils.hasText(authenticatedToken.getScope())) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + authenticatedToken.getScope()));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtAuthException e) {
                markAuthenticationFailure(request, e.getError(), e);
            } catch (ExpiredJwtException e) {
                markAuthenticationFailure(request, JwtAuthError.EXPIRED, e);
            } catch (JwtException | IllegalArgumentException e) {
                markAuthenticationFailure(request, JwtAuthError.INVALID, e);
            } catch (Exception e) {
                markAuthenticationFailure(request, JwtAuthError.INVALID, e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void markAuthenticationFailure(HttpServletRequest request, JwtAuthError error, Exception exception) {
        String requestUri = request.getRequestURI();
        String clientIp = request.getRemoteAddr();
        request.setAttribute(JwtAuthError.REQUEST_ATTRIBUTE, error);
        log.warn(
                "JWT authentication failed for request: {} (clientIp: {}, reason: {})",
                requestUri,
                clientIp,
                error.headerValue(),
                exception);
        SecurityContextHolder.clearContext();
    }
}
