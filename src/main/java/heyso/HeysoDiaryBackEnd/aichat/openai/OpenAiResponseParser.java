package heyso.HeysoDiaryBackEnd.aichat.openai;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class OpenAiResponseParser {
    private OpenAiResponseParser() {
    }

    public static String extractRequestId(JsonNode root) {
        // Responses API 응답의 id를 request_id로 저장
        JsonNode id = root.get("id");
        return id != null && !id.isNull() ? id.asText() : null;
    }

    public static Usage extractUsage(JsonNode root) {
        JsonNode usage = root.get("usage");
        if (usage == null || usage.isNull())
            return new Usage(null, null, null);

        // 문서/스냅샷에 따라 키가 다를 수 있어서 방어적으로 처리
        Integer inputTokens = getInt(usage, "input_tokens", "prompt_tokens");
        Integer outputTokens = getInt(usage, "output_tokens", "completion_tokens");
        Integer totalTokens = getInt(usage, "total_tokens");

        return new Usage(inputTokens, outputTokens, totalTokens);
    }

    private static Integer getInt(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && v.isNumber())
                return v.asInt();
        }
        return null;
    }

    /**
     * 1) output_text 필드가 있으면 그걸 우선 사용
     * 2) 없으면 output[]를 순회하며 message/content에서 text를 합침
     */
    public static String extractAssistantText(JsonNode root) {
        JsonNode shortcut = root.get("output_text");
        if (shortcut != null && shortcut.isTextual()) {
            return shortcut.asText();
        }

        StringBuilder sb = new StringBuilder();
        JsonNode output = root.get("output");
        if (output == null || !output.isArray())
            return "";

        for (JsonNode item : output) {
            // Responses API에서 message item을 찾는다
            JsonNode type = item.get("type");
            if (type != null && "message".equals(type.asText())) {
                JsonNode role = item.get("role");
                if (role != null && "assistant".equals(role.asText())) {
                    JsonNode contentArr = item.get("content");
                    if (contentArr != null && contentArr.isArray()) {
                        Iterator<JsonNode> it = contentArr.elements();
                        while (it.hasNext()) {
                            JsonNode c = it.next();
                            // output_text 타입이 흔함
                            JsonNode cType = c.get("type");
                            if (cType != null
                                    && ("output_text".equals(cType.asText()) || "text".equals(cType.asText()))) {
                                JsonNode text = c.get("text");
                                if (text != null && text.isTextual()) {
                                    if (sb.length() > 0)
                                        sb.append("\n");
                                    sb.append(text.asText());
                                }
                            }
                        }
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    public record Usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
    }

}
