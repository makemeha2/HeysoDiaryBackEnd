package heyso.HeysoDiaryBackEnd.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatAssistantReplyRequest {
    @NotBlank
    @Size(max = 20000)
    private String userContent;

    // tb_chat_message.uk_tb_chat_msg_client (conversation_id, client_message_id) 대응
    @Size(max = 64)
    private String userClientMessageId;

    // 스레드 구조를 쓰고 싶을 때(선택)
    private Long parentMessageId;

    // 출력 포맷(현재는 markdown 기본)
    // 필요하면 "text"도 받도록 확장 가능
    private String assistantContentFormat = "markdown";

}
