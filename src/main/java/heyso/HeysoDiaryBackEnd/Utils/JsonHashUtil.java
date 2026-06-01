package heyso.HeysoDiaryBackEnd.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonHashUtil {

    private JsonHashUtil() {
    }

    public static String toJson(ObjectMapper objectMapper, Object value,
            Function<Exception, ? extends RuntimeException> exceptionMapper) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw exceptionMapper.apply(e);
        }
    }

    public static String toJsonOrNull(ObjectMapper objectMapper, JsonNode node,
            Function<Exception, ? extends RuntimeException> exceptionMapper) {
        if (node == null || node.isNull()) {
            return null;
        }
        return toJson(objectMapper, node, exceptionMapper);
    }

    public static String sha256Hex(String value) {
        return sha256Hex(value, e -> new IllegalStateException("SHA-256 digest is not available", e));
    }

    public static String sha256Hex(String value, Function<Exception, ? extends RuntimeException> exceptionMapper) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw exceptionMapper.apply(e);
        }
    }
}
