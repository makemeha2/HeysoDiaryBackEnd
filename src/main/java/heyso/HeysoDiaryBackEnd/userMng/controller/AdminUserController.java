package heyso.HeysoDiaryBackEnd.userMng.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserCreateRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserPageResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserPasswordResetRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserSearchRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserStatusUpdateRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserUpdateRequest;
import heyso.HeysoDiaryBackEnd.userMng.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<AdminUserPageResponse> getAdminUserPage(@Valid AdminUserSearchRequest request) {
        return ResponseEntity.ok(adminUserService.getAdminUserPage(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponse> getAdminUserDetail(@PathVariable @Positive Long userId) {
        return ResponseEntity.ok(adminUserService.getAdminUserDetail(userId));
    }

    @PostMapping
    public ResponseEntity<AdminUserDetailResponse> createAdminUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminUserService.createAdminUser(request));
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<AdminUserDetailResponse> updateAdminUser(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.updateAdminUser(userId, request));
    }

    @PostMapping("/{userId}/status")
    public ResponseEntity<AdminUserDetailResponse> updateAdminUserStatus(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.updateAdminUserStatus(userId, request));
    }

    @PostMapping("/{userId}/password")
    public ResponseEntity<Void> updateAdminUserPassword(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AdminUserPasswordResetRequest request) {
        adminUserService.updateAdminUserPassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/delete")
    public ResponseEntity<Void> deleteAdminUser(@PathVariable @Positive Long userId) {
        adminUserService.deleteAdminUser(userId);
        return ResponseEntity.ok().build();
    }
}
