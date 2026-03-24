package heyso.HeysoDiaryBackEnd.ai.client;

import java.util.List;

import lombok.Builder;

@Builder
public record AiRequest(
        AiProvider provider,
        String model,
        List<AiMessage> messages,
        Double temperature,
        Double topP,
        Integer maxTokens) {
}
