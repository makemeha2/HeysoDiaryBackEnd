package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatConversationListResponse {
    private List<ChatConversationListItem> conversations;

    public static ChatConversationListResponse of(List<ChatConversationListItem> conversations) {
        return new ChatConversationListResponse(conversations);
    }
}
