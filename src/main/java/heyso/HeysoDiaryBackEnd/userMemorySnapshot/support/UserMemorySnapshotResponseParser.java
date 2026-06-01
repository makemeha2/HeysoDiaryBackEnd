package heyso.HeysoDiaryBackEnd.userMemorySnapshot.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotParsedResponse;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.type.UserMemorySnapshotException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMemorySnapshotResponseParser {
    private final ObjectMapper objectMapper;

    public UserMemorySnapshotParsedResponse parse(String content) {
        JsonNode root = readTree(stripCodeFence(content));
        if (root == null || !root.isObject()) {
            throw invalid("snapshot response root must be a JSON object");
        }
        String summary = text(root, "summary");
        if (StringUtils.isBlank(summary)) {
            throw invalid("summary is required");
        }

        return new UserMemorySnapshotParsedResponse(
                summary.trim(),
                containerJson(root, "recurring_themes"),
                containerJson(root, "important_people"),
                containerJson(root, "stress_factors"),
                containerJson(root, "recovery_factors"),
                containerJson(root, "trait_summary"));
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new UserMemorySnapshotException("AI user memory snapshot response is not valid JSON", e);
        }
    }

    private String stripCodeFence(String content) {
        String trimmed = StringUtils.defaultString(content).trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
            return trimmed.substring(firstLineEnd + 1, lastFence).trim();
        }
        return trimmed;
    }

    private String text(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node == null || node.isNull() ? null : node.asText();
    }

    private String containerJson(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull() || !node.isContainerNode()) {
            throw invalid(fieldName + " must be a JSON array or object");
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw invalid(fieldName + " is invalid JSON");
        }
    }

    private UserMemorySnapshotException invalid(String message) {
        return new UserMemorySnapshotException(message);
    }
}
