package heyso.HeysoDiaryBackEnd.ai.support;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.client.AiClient;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;

@Component
public class AiCallExecutor {

    private final Map<AiProvider, AiClient> clientsByProvider = new EnumMap<>(AiProvider.class);

    public AiCallExecutor(List<AiClient> clients) {
        for (AiClient client : clients) {
            clientsByProvider.put(client.provider(), client);
        }
    }

    public AiResponse call(AiRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI request is required");
        }

        AiProvider provider = request.provider() == null ? AiProvider.OPENAI : request.provider();
        AiClient client = clientsByProvider.get(provider);
        if (client == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_IMPLEMENTED,
                    "AI provider is not configured: " + provider);
        }

        return client.generate(request);
    }
}
