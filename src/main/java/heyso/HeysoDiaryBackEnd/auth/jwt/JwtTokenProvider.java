package heyso.HeysoDiaryBackEnd.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    @Value("${app.auth.jwt-secret}")
    private String secretKeyPlain;

    @Value("${app.auth.jwt-expiration-ms}")
    private long validityInMs;

    @Value("${app.auth.jwt-issuer:heyso-diary}")
    private String issuer;

    @Value("${app.auth.jwt-audience:heyso-diary-web}")
    private String audience;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String email, String role) {
        return generateToken(userId, email, role, null);
    }

    public String generateToken(Long userId, String email, String role, String scope) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (scope != null && !scope.isBlank()) {
            builder.claim("scope", scope);
        }

        return builder.compact();
    }

    public Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token);
    }
}
