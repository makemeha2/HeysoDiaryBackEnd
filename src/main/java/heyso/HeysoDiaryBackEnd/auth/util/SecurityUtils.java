package heyso.HeysoDiaryBackEnd.auth.util;

import heyso.HeysoDiaryBackEnd.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof User) {
            return Optional.of((User) principal);
        }
        return Optional.empty();
    }

    public static User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
