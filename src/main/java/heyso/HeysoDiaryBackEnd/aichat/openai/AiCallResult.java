package heyso.HeysoDiaryBackEnd.aichat.openai;

public record AiCallResult(
        String content,
        String requestId,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens) {
}
