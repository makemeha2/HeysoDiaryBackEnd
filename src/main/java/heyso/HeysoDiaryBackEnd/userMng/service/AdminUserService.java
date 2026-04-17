package heyso.HeysoDiaryBackEnd.userMng.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserCreateRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailRow;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserListRow;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserPageResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserPasswordResetRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserSearchRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserStatusUpdateRequest;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserUpdateRequest;
import heyso.HeysoDiaryBackEnd.userMng.exception.AdminUserConflictException;
import heyso.HeysoDiaryBackEnd.userMng.mapper.AdminUserMapper;
import heyso.HeysoDiaryBackEnd.userMng.mapper.AdminUserResponseMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final String PROVIDER_LOCAL = "LOCAL";

    private final AdminAuthorizationService adminAuthorizationService;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserResponseMapper adminUserResponseMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public AdminUserPageResponse getAdminUserPage(AdminUserSearchRequest request) {
        adminAuthorizationService.requireAdminUser();

        List<AdminUserListRow> rows = adminUserMapper.selectAdminUserPage(request);
        long totalCount = adminUserMapper.countAdminUsers(request);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / request.getSize());

        return AdminUserPageResponse.builder()
                .items(adminUserResponseMapper.toListResponses(rows))
                .page(request.getPage())
                .size(request.getSize())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getAdminUserDetail(Long userId) {
        adminAuthorizationService.requireAdminUser();

        AdminUserDetailRow detailRow = getAdminUserOrThrow(userId);
        return adminUserResponseMapper.toDetailResponse(detailRow);
    }

    @Transactional
    public AdminUserDetailResponse createAdminUser(AdminUserCreateRequest request) {
        adminAuthorizationService.requireAdminUser();

        String normalizedEmail = StringUtils.trim(request.getEmail());
        String normalizedNickname = StringUtils.trim(request.getNickname());
        String normalizedLoginId = StringUtils.trim(request.getLoginId());

        ensureEmailAvailable(normalizedEmail, null);
        ensureLoginIdAvailable(normalizedLoginId);

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setNickname(normalizedNickname);
        user.setRole(request.getRole());
        user.setStatus(STATUS_ACTIVE);
        userMapper.insertUser(user);

        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(user.getUserId());
        userAuth.setAuthProvider(PROVIDER_LOCAL);
        userAuth.setProviderUserId(normalizedLoginId);
        userAuth.setLoginId(normalizedLoginId);
        userAuth.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userMapper.insertUserAuth(userAuth);

        return getAdminUserDetailResponse(user.getUserId());
    }

    @Transactional
    public AdminUserDetailResponse updateAdminUser(Long userId, AdminUserUpdateRequest request) {
        User operator = adminAuthorizationService.requireAdminUser();
        AdminUserDetailRow targetUser = getMutableAdminUserOrThrow(userId);

        String normalizedNickname = StringUtils.trim(request.getNickname());

        validateDemotion(operator, targetUser, request.getRole());
        validateLastActiveAdminProtection(targetUser, request.getRole(), targetUser.getStatus());

        int updated = adminUserMapper.updateAdminUser(userId, normalizedNickname, request.getRole());
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user");
        }

        return getAdminUserDetailResponse(userId);
    }

    @Transactional
    public AdminUserDetailResponse updateAdminUserStatus(Long userId, AdminUserStatusUpdateRequest request) {
        User operator = adminAuthorizationService.requireAdminUser();
        AdminUserDetailRow targetUser = getMutableAdminUserOrThrow(userId);

        validateStatusChange(operator, targetUser, request.getStatus());
        validateLastActiveAdminProtection(targetUser, targetUser.getRole(), request.getStatus());

        int updated = adminUserMapper.updateAdminUserStatus(userId, request.getStatus());
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user status");
        }

        return getAdminUserDetailResponse(userId);
    }

    @Transactional
    public void updateAdminUserPassword(Long userId, AdminUserPasswordResetRequest request) {
        adminAuthorizationService.requireAdminUser();
        AdminUserDetailRow targetUser = getMutableAdminUserOrThrow(userId);

        ensureLocalUser(targetUser);

        int updated = adminUserMapper.updateLocalUserPassword(userId, passwordEncoder.encode(request.getNewPassword()));
        if (updated <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user password");
        }
    }

    @Transactional
    public void deleteAdminUser(Long userId) {
        User operator = adminAuthorizationService.requireAdminUser();
        AdminUserDetailRow targetUser = getMutableAdminUserOrThrow(userId);

        if (operator.getUserId().equals(userId)) {
            throw new AdminUserConflictException("CANNOT_DELETE_SELF", "You cannot delete your own account");
        }
        ensureLocalUser(targetUser);
        validateLastActiveAdminProtection(targetUser, null, null);

        userMapper.deleteUserAuthByUserId(userId);
        int deleted = adminUserMapper.deleteUserById(userId);
        if (deleted <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user");
        }
    }

    private AdminUserDetailRow getAdminUserOrThrow(Long userId) {
        AdminUserDetailRow detailRow = adminUserMapper.selectAdminUserDetail(userId);
        if (detailRow == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return detailRow;
    }

    private AdminUserDetailResponse getAdminUserDetailResponse(Long userId) {
        return adminUserResponseMapper.toDetailResponse(getAdminUserOrThrow(userId));
    }

    private AdminUserDetailRow getMutableAdminUserOrThrow(Long userId) {
        AdminUserDetailRow detailRow = getAdminUserOrThrow(userId);
        if (STATUS_WITHDRAWN.equals(detailRow.getStatus())) {
            throw new AdminUserConflictException("WITHDRAWN_USER_IMMUTABLE", "Withdrawn user cannot be modified");
        }
        return detailRow;
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        User existingUser = userMapper.selectUserByEmail(email);
        if (existingUser != null && !existingUser.getUserId().equals(currentUserId)) {
            throw new AdminUserConflictException("EMAIL_DUPLICATED", "Email already exists");
        }
    }

    private void ensureLoginIdAvailable(String loginId) {
        UserAuth existingAuth = userMapper.selectUserAuthByLoginIdAndProvider(loginId, PROVIDER_LOCAL);
        if (existingAuth != null) {
            throw new AdminUserConflictException("LOGIN_ID_DUPLICATED", "Login ID already exists");
        }
    }

    private void ensureLocalUser(AdminUserDetailRow targetUser) {
        if (!PROVIDER_LOCAL.equals(targetUser.getAuthProvider())) {
            throw new AdminUserConflictException("ONLY_LOCAL_ALLOWED", "Only LOCAL users are allowed for this action");
        }
    }

    private void validateDemotion(User operator, AdminUserDetailRow targetUser, String nextRole) {
        if (operator.getUserId().equals(targetUser.getUserId())
                && ROLE_ADMIN.equals(targetUser.getRole())
                && !ROLE_ADMIN.equals(nextRole)) {
            throw new AdminUserConflictException("CANNOT_DEMOTE_SELF", "You cannot demote your own role");
        }
    }

    private void validateStatusChange(User operator, AdminUserDetailRow targetUser, String nextStatus) {
        if (operator.getUserId().equals(targetUser.getUserId()) && !STATUS_ACTIVE.equals(nextStatus)) {
            throw new AdminUserConflictException("CANNOT_DEACTIVATE_SELF", "You cannot deactivate your own account");
        }
    }

    private void validateLastActiveAdminProtection(AdminUserDetailRow targetUser, String nextRole, String nextStatus) {
        boolean currentActiveAdmin = ROLE_ADMIN.equals(targetUser.getRole())
                && STATUS_ACTIVE.equals(targetUser.getStatus());
        if (!currentActiveAdmin) {
            return;
        }

        String resolvedRole = nextRole != null ? nextRole : targetUser.getRole();
        String resolvedStatus = nextStatus != null ? nextStatus : targetUser.getStatus();
        boolean remainsActiveAdmin = ROLE_ADMIN.equals(resolvedRole) && STATUS_ACTIVE.equals(resolvedStatus);

        if (!remainsActiveAdmin && adminUserMapper.countOtherActiveAdmins(targetUser.getUserId()) == 0) {
            throw new AdminUserConflictException("LAST_ADMIN_PROTECTED", "The last active admin is protected");
        }
    }

    // private String normalize(String value) {
    // return value);
    // }
}
