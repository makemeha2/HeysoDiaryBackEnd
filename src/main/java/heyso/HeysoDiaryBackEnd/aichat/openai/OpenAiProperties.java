package heyso.HeysoDiaryBackEnd.aichat.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    /**
     * 예: sk-...
     */
    private String apiKey;

    /**
     * 기본값: https://api.openai.com
     */
    private String baseUrl = "https://api.openai.com";

    /**
     * 타임아웃(초) - 필요 시 사용
     */
    private int timeoutSeconds = 30;

    /**
     * assistant-reply 생성 시 최근 몇 개 메시지를 문맥으로 보낼지
     */
    private int contextMessageLimit = 10;

    /**
     * summary 가 있을 때 developer 메시지로 넣을 prefix
     */
    private String summaryPrefix = "Conversation summary:\n";
}
