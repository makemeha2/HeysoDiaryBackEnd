package heyso.HeysoDiaryBackEnd.auth.security;

import jakarta.servlet.http.HttpServletRequest;

public final class PublicAuthEndpoints {
    public static final String GOOGLE_OAUTH_LOGIN = "/api/auth/oauth/google";
    public static final String TOKEN_VALIDATE = "/api/auth/validate";
    public static final String ADMIN_LOGIN = "/api/admin/auth/login";

    public static final String[] PERMIT_ALL = {
            GOOGLE_OAUTH_LOGIN,
            TOKEN_VALIDATE,
            ADMIN_LOGIN,
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] JWT_FILTER_EXCLUDED = {
            GOOGLE_OAUTH_LOGIN,
            TOKEN_VALIDATE,
            ADMIN_LOGIN
    };

    private PublicAuthEndpoints() {
    }

    public static boolean isJwtFilterExcluded(HttpServletRequest request) {
        return matches(request, JWT_FILTER_EXCLUDED);
    }

    public static boolean isCsrfExcluded(HttpServletRequest request) {
        return matches(request, JWT_FILTER_EXCLUDED);
    }

    public static boolean isApiRequest(HttpServletRequest request) {
        String path = requestPath(request);
        return path != null && path.startsWith("/api/");
    }

    private static boolean matches(HttpServletRequest request, String[] paths) {
        String path = requestPath(request);
        for (String candidate : paths) {
            if (candidate.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (uri != null && contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }
}
