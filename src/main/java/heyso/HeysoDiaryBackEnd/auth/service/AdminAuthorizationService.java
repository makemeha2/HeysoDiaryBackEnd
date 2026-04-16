package heyso.HeysoDiaryBackEnd.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;

@Service
public class AdminAuthorizationService {

    private static final String ADMIN_SCOPE_AUTHORITY = "SCOPE_admin";

    public User requireAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminScope = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> ADMIN_SCOPE_AUTHORITY.equals(authority.getAuthority()));
        if (!hasAdminScope) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin scope required");
        }

        User user = SecurityUtils.getCurrentUserOrThrow();
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user;
    }

    public Long requireAdminUserId() {
        return requireAdminUser().getUserId();
    }
}
