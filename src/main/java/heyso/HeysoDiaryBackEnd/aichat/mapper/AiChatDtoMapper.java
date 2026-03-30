package heyso.HeysoDiaryBackEnd.aichat.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListItem;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryResponse;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversationSummary;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatMessage;

@Component
public class AiChatDtoMapper {

    // ========== Conversation ==========
    public ChatConversationListItem toConversationListItem(ChatConversation conversation) {
        if (conversation == null) {
            return null;
        }

        return new ChatConversationListItem(
                conversation.getConversationId(),
                conversation.getTitle(),
                conversation.getModel(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }

    public List<ChatConversationListItem> toConversationListItems(List<ChatConversation> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            return Collections.emptyList();
        }
        return conversations.stream()
                .map(this::toConversationListItem)
                .collect(Collectors.toList());
    }

    // ========== Message ==========
    public ChatMessageResponse toMessageResponse(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return new ChatMessageResponse(
                message.getMessageId(),
                message.getConversationId(),
                message.getRole(),
                message.getContent(),
                message.getContentFormat(),
                message.getTokenCount(),
                message.getParentMessageId(),
                message.getClientMessageId(),
                message.getCreatedAt());
    }

    public List<ChatMessageResponse> toMessageResponses(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    // ========== Summary ==========
    public ChatSummaryResponse toSummaryResponse(ChatConversationSummary summary) {
        if (summary == null) {
            return null;
        }
        return new ChatSummaryResponse(
                summary.getConversationId(),
                summary.getSummary(),
                summary.getSummaryVersion(),
                summary.getLastMessageId(),
                summary.getUpdatedAt());
    }
}
