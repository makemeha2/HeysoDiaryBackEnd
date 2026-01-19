package heyso.HeysoDiaryBackEnd.aichat.openai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiClient {
    private final OpenAiProperties props;
    private final ObjectMapper objectMapper;

    /**
     * Responses API 호출
     * POST {baseUrl}/v1/responses
     *
     * input: [{role, content}, ...] 형태 지원 :contentReference[oaicite:1]{index=1}
     */
    public JsonNode createResponse(String model, List<Map<String, Object>> input) {
        RestClient restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", input);
        body.put("store", false); // 서버에 OpenAI 쪽 저장 불필요하면 false
        body.put("truncation", "auto"); // 컨텍스트 초과 시 자동 절단(옵션) :contentReference[oaicite:2]{index=2}

        // 필요 시 max_output_tokens / temperature 등도 여기서 세팅 가능
        // body.put("max_output_tokens", 800);

        return restClient.post()
                .uri("/v1/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + props.getApiKey())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }
}
