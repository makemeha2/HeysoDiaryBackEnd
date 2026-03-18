package heyso.HeysoDiaryBackEnd.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class MonitoringSecurityJsonUtil {

    private MonitoringSecurityJsonUtil() {
    }

    public static String buildDetailJson(HttpServletRequest request, String extraKey, String extraValue) {
        String uri = request != null ? request.getRequestURI() : null;
        String method = request != null ? request.getMethod() : null;

        return "{"
                + "\"uri\":\"" + escapeJson(uri) + "\","
                + "\"method\":\"" + escapeJson(method) + "\","
                + "\"" + escapeJson(extraKey) + "\":\"" + escapeJson(extraValue) + "\""
                + "}";
    }

    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
