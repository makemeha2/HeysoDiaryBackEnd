package heyso.HeysoDiaryBackEnd.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthError;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthException;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.auth.mapper.AuthTokenDenylistMapper;
import heyso.HeysoDiaryBackEnd.auth.model.AuthTokenDenylistEntry;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private static final String REVOKED_REASON_LOGOUT = "LOGOUT";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthTokenDenylistMapper denylistMapper;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AuthenticatedToken authenticate(String token) {
        Jws<Claims> claimsJws = jwtTokenProvider.parseClaims(token);
        Claims claims = claimsJws.getBody();
        String jti = claims.getId();
        if (!StringUtils.hasText(jti)) {
            throw new JwtAuthException(JwtAuthError.INVALID, "JWT ID is missing");
        }

        LocalDateTime now = LocalDateTime.now();
        if (denylistMapper.existsActiveJti(jti, now)) {
            throw new JwtAuthException(JwtAuthError.REVOKED, "JWT has been revoked");
        }

        Long userId = parseUserId(claims);
        User user = userMapper.selectUserById(userId);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new JwtAuthException(JwtAuthError.INACTIVE, "User is not active");
        }
        if (isRevokedByUserPolicy(user, claims)) {
            throw new JwtAuthException(JwtAuthError.REVOKED, "JWT issued before user revocation time");
        }

        String role = (String) claims.get("role");
        String scope = (String) claims.get("scope");
        return new AuthenticatedToken(claims, user, role, scope);
    }

    @Transactional
    public void revokeAccessToken(String token, String reason) {
        AuthenticatedToken authenticatedToken = authenticate(token);
        Claims claims = authenticatedToken.getClaims();
        AuthTokenDenylistEntry entry = new AuthTokenDenylistEntry();
        entry.setJti(claims.getId());
        entry.setUserId(authenticatedToken.getUser().getUserId());
        entry.setExpiresAt(toLocalDateTime(claims.getExpiration().toInstant()));
        entry.setRevokedReason(StringUtils.hasText(reason) ? reason : REVOKED_REASON_LOGOUT);
        denylistMapper.insertIgnore(entry);
        denylistMapper.deleteExpired(LocalDateTime.now());
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        userMapper.updateTokenRevokedAfter(userId, LocalDateTime.now());
    }

    public String logoutReason() {
        return REVOKED_REASON_LOGOUT;
    }

    private Long parseUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException | NullPointerException e) {
            throw new JwtAuthException(JwtAuthError.INVALID, "JWT subject is invalid");
        }
    }

    private boolean isRevokedByUserPolicy(User user, Claims claims) {
        if (user.getTokenRevokedAfter() == null || claims.getIssuedAt() == null) {
            return false;
        }
        LocalDateTime issuedAt = toLocalDateTime(claims.getIssuedAt().toInstant());
        return !issuedAt.isAfter(user.getTokenRevokedAfter());
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
