package heyso.HeysoDiaryBackEnd.ai.client;

public record AiResponse(
        String content,
        AiProvider provider,
        String model,
        String requestId,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens) {
}
