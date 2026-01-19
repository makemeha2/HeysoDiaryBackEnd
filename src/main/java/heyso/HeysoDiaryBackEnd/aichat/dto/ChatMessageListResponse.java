package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessageListResponse {
    private List<ChatMessageResponse> messages;

    public static ChatMessageListResponse of(List<ChatMessageResponse> messages) {
        return new ChatMessageListResponse(messages);
    }
}
