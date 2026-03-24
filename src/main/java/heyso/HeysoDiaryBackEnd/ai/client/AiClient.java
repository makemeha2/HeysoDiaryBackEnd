package heyso.HeysoDiaryBackEnd.ai.client;

public interface AiClient {

    AiProvider provider();

    AiResponse generate(AiRequest request);
}
