package heyso.HeysoDiaryBackEnd.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class JsonHashUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toJsonRendersWithProvidedObjectMapper() {
        String json = JsonHashUtil.toJson(objectMapper, Map.of("name", "heyso"),
                e -> new IllegalStateException("json failed", e));

        assertThat(json).isEqualTo("{\"name\":\"heyso\"}");
    }

    @Test
    void toJsonOrNullReturnsNullForNullNode() {
        String json = JsonHashUtil.toJsonOrNull(objectMapper, null, e -> new IllegalStateException(e));

        assertThat(json).isNull();
    }

    @Test
    void sha256HexReturnsLowercaseHex() {
        String hash = JsonHashUtil.sha256Hex("abc");

        assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sha256HexUsesExceptionMapper() {
        assertThatThrownBy(() -> JsonHashUtil.sha256Hex(null, e -> new IllegalArgumentException("hash failed", e)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("hash failed");
    }
}
