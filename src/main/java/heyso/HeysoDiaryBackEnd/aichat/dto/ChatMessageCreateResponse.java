package heyso.HeysoDiaryBackEnd.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageCreateResponse {
    private Long messageId;

    public static ChatMessageCreateResponse of(Long messageId) {
        return new ChatMessageCreateResponse(messageId);
    }
}
