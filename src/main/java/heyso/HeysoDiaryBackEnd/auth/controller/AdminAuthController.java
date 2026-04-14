package heyso.HeysoDiaryBackEnd.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.auth.dto.AdminLoginRequest;
import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request.getLoginId(), request.getPassword()));
    }
}
