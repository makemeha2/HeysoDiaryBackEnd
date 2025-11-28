package heyso.HeysoDiaryBackEnd.auth.controller;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.dto.GoogleLoginRequest;
import heyso.HeysoDiaryBackEnd.auth.service.GoogleOAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = googleOAuthService.loginOrRegister(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}
