package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatConversationCreateResponse {
    private Long conversationId;

    public static ChatConversationCreateResponse of(Long conversationId) {
        return new ChatConversationCreateResponse(conversationId);
    }
}
