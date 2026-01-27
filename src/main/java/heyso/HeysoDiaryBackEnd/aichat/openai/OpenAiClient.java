package heyso.HeysoDiaryBackEnd.aichat.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final ChatClient chatClient;

    /**
     * Spring AI ChatClient 기반 호출
     * 
     * @param modelName gpt-4o-mini 등
     * @param messages  (role, content) 형태의 메시지들
     * @return assistant 텍스트
     */
    public CallResponseSpec createResponseSpec(String modelName, List<RoleMessage> messages) {
        return createResponseSpec(modelName, messages, null, null, null);
    }

    /**
     * 옵션(temperature, topP, maxTokens)을 포함한 호출
     */
    public CallResponseSpec createResponseSpec(String modelName,
                                               List<RoleMessage> messages,
                                               BigDecimal temperature,
                                               BigDecimal topP,
                                               Integer maxOutputTokens) {
        List<Message> springMessages = new ArrayList<>();

        for (RoleMessage m : messages) {
            if (m == null || m.role() == null)
                continue;
            String role = m.role();

            switch (role) {
                case "developer", "system" -> springMessages.add(new SystemMessage(m.content()));
                case "user" -> springMessages.add(new UserMessage(m.content()));
                case "assistant" -> springMessages.add(new AssistantMessage(m.content()));
                default -> {
                    // tool 등은 이번 버전에서는 무시
                }
            }
        }

        ChatOptions.Builder optionsBuilder = ChatOptions.builder().model(modelName);

        if (temperature != null) {
            optionsBuilder.temperature(temperature.doubleValue());
        }
        if (topP != null) {
            optionsBuilder.topP(topP.doubleValue());
        }
        if (maxOutputTokens != null) {
            optionsBuilder.maxTokens(maxOutputTokens);
        }

        CallResponseSpec response = chatClient.prompt()
                .messages(springMessages)
                .options(optionsBuilder.build())
                .call();

        // Spring AI가 최종 assistant content를 문자열로 제공
        return response;
    }

    /**
     * 기존 코드와 호환되도록 간단한 record를 둠
     */
    public record RoleMessage(String role, String content) {
    }
}

/*
 * 과거코드 (혹시 몰라 백업)
 * public class OpenAiClient {
 * private final OpenAiProperties props;
 * private final ObjectMapper objectMapper;
 * 
 * public JsonNode createResponse(String model, List<Map<String, Object>> input)
 * {
 * RestClient restClient = RestClient.builder()
 * .baseUrl(props.getBaseUrl())
 * .build();
 * 
 * Map<String, Object> body = new HashMap<>();
 * body.put("model", model);
 * body.put("input", input);
 * body.put("store", false); // 서버에 OpenAI 쪽 저장 불필요하면 false
 * body.put("truncation", "auto"); // 컨텍스트 초과 시 자동 절단(옵션)
 * :contentReference[oaicite:2]{index=2}
 * 
 * // 필요 시 max_output_tokens / temperature 등도 여기서 세팅 가능
 * // body.put("max_output_tokens", 800);
 * 
 * return restClient.post()
 * .uri("/v1/responses")
 * .contentType(MediaType.APPLICATION_JSON)
 * .accept(MediaType.APPLICATION_JSON)
 * .header("Authorization", "Bearer " + props.getApiKey())
 * .body(body)
 * .retrieve()
 * .body(JsonNode.class);
 * }
 * }
 */
