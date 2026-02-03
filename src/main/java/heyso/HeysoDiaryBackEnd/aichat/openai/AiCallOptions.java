package heyso.HeysoDiaryBackEnd.aichat.openai;

import java.util.Optional;

public class AiCallOptions {
    private final Double temperature;
    private final Double topP;
    private final Integer maxTokens;

    private AiCallOptions(Double temperature, Double topP, Integer maxTokens) {
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
    }

    public static AiCallOptions of(Double temperature, Double topP, Integer maxTokens) {
        return new AiCallOptions(temperature, topP, maxTokens);
    }

    public static AiCallOptions empty() {
        return new AiCallOptions(null, null, null);
    }

    public Optional<Double> temperature() {
        return Optional.ofNullable(temperature);
    }

    public Optional<Double> topP() {
        return Optional.ofNullable(topP);
    }

    public Optional<Integer> maxTokens() {
        return Optional.ofNullable(maxTokens);
    }
}
