package heyso.HeysoDiaryBackEnd.aichat.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListItem;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryResponse;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversationSummary;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatMessage;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AiChatDtoMapper {

    // ========== Conversation ==========
    ChatConversationListItem toConversationListItem(ChatConversation conversation);

    List<ChatConversationListItem> toConversationListItems(List<ChatConversation> conversations);

    // ========== Message ==========
    ChatMessageResponse toMessageResponse(ChatMessage message);

    List<ChatMessageResponse> toMessageResponses(List<ChatMessage> messages);

    // ========== Summary ==========
    ChatSummaryResponse toSummaryResponse(ChatConversationSummary summary);
}
