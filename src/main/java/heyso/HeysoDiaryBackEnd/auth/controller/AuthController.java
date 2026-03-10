package heyso.HeysoDiaryBackEnd.auth.controller;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.GoogleLoginRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.GoogleOAuthReauthRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.ReauthVerifyResponse;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.auth.service.GoogleOAuthService;
import heyso.HeysoDiaryBackEnd.auth.service.OAuthReauthService;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import jakarta.validation.Valid;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleOAuthService googleOAuthService;
    private final OAuthReauthService oAuthReauthService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = googleOAuthService.loginOrRegister(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reauth/oauth/google")
    public ResponseEntity<ReauthVerifyResponse> googleReauthForWithdraw(
            @Valid @RequestBody GoogleOAuthReauthRequest request) {
        Long userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        ReauthVerifyResponse response = oAuthReauthService.verifyGoogleForWithdraw(userId, request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Token is Not Exist.");
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        try {
            jwtTokenProvider.parseClaims(token);
            log.error("Token is correct");
            return ResponseEntity.ok().build();
        } catch (JwtException | IllegalArgumentException e) {
            log.error(String.format("토큰이 잘못되었습니다.{}", token));
            return ResponseEntity.status(401).build();
        }
    }
}
