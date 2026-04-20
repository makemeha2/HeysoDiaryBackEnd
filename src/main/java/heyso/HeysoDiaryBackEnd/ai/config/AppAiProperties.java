package heyso.HeysoDiaryBackEnd.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AppAiProperties {

    private AiProvider defaultProvider = AiProvider.OPENAI;

    private String defaultChatModel = "gpt-4o-mini";

    private String defaultSummaryModel = "gpt-4o-2024-08-06";

    private String defaultDiaryCommentModel = "gpt-4o";

    private String defaultDiaryPolishModel = "gpt-4o-mini";

    private String defaultDiaryNudgeModel = "gpt-4o";

    private int contextMessageLimit = 10;

    private String summaryPrefix = "Conversation summary:\n";
}
