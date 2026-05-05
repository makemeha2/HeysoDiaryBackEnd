package heyso.HeysoDiaryBackEnd.auth.controller;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.EmailReauthSendResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.EmailReauthVerifyRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.GoogleLoginRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.AccountWithdrawRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.ReauthStatusResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.ReauthVerifyResponse;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthError;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthException;
import heyso.HeysoDiaryBackEnd.auth.service.AuthTokenService;
import heyso.HeysoDiaryBackEnd.auth.service.AccountDeleteService;
import heyso.HeysoDiaryBackEnd.auth.service.EmailReauthService;
import heyso.HeysoDiaryBackEnd.auth.service.GoogleOAuthService;
import heyso.HeysoDiaryBackEnd.auth.service.ReauthPurpose;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
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
    private final EmailReauthService emailReauthService;
    private final AccountDeleteService accountDeleteService;
    private final AuthTokenService authTokenService;

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = googleOAuthService.loginOrRegister(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reauth/email/send")
    public ResponseEntity<EmailReauthSendResponse> sendEmailOtpForAccountDelete(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        EmailReauthSendResponse response = emailReauthService.sendOtpForAccountDelete(
                userId,
                extractClientIp(request),
                request.getHeader(HttpHeaders.USER_AGENT));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reauth/email/verify")
    public ResponseEntity<ReauthVerifyResponse> verifyEmailOtpForAccountDelete(
            @Valid @RequestBody EmailReauthVerifyRequest request,
            HttpServletRequest servletRequest) {
        Long userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
                
        ReauthVerifyResponse response = emailReauthService.verifyOtpForAccountDelete(
                userId,
                request.getOtp(),
                extractClientIp(servletRequest),
                servletRequest.getHeader(HttpHeaders.USER_AGENT));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reauth/status")
    public ResponseEntity<ReauthStatusResponse> getReauthStatus(
            @RequestParam(value = "purpose", defaultValue = "ACCOUNT_DELETE") ReauthPurpose purpose) {
        Long userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        ReauthStatusResponse response = emailReauthService.getReauthStatus(userId, purpose);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdrawAccount(@Valid @RequestBody(required = false) AccountWithdrawRequest request) {
        Long userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        accountDeleteService.deleteAccount(
                userId,
                request != null ? request.getReasonCode() : null,
                request != null ? request.getReasonText() : null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractBearerToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(JwtAuthError.RESPONSE_HEADER, JwtAuthError.INVALID.headerValue())
                    .build();
        }

        authTokenService.revokeAccessToken(token, authTokenService.logoutReason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractBearerToken(authHeader);
        if (token == null) {
            log.error("Token is Not Exist.");
            return ResponseEntity.status(401)
                    .header(JwtAuthError.RESPONSE_HEADER, JwtAuthError.INVALID.headerValue())
                    .build();
        }

        try {
            authTokenService.authenticate(token);
            log.error("Token is correct");
            return ResponseEntity.ok().build();
        } catch (JwtAuthException e) {
            log.error("Token is rejected. reason={}", e.getError().headerValue());
            return ResponseEntity.status(401)
                    .header(JwtAuthError.RESPONSE_HEADER, e.getError().headerValue())
                    .build();
        } catch (ExpiredJwtException e) {
            log.error("Token is expired.");
            return ResponseEntity.status(401)
                    .header(JwtAuthError.RESPONSE_HEADER, JwtAuthError.EXPIRED.headerValue())
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token is invalid.");
            return ResponseEntity.status(401)
                    .header(JwtAuthError.RESPONSE_HEADER, JwtAuthError.INVALID.headerValue())
                    .build();
        }
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] ips = forwardedFor.split(",");
            return ips[0].trim();
        }
        return request.getRemoteAddr();
    }
}
