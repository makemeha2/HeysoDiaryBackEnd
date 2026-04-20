package heyso.HeysoDiaryBackEnd.ai.provider.claude;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeConfig {

    @Bean("claudeChatClient")
    public ChatClient claudeChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
