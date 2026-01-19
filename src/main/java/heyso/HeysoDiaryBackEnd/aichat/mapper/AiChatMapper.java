package heyso.HeysoDiaryBackEnd.aichat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversationSummary;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatMessage;

@Mapper
public interface AiChatMapper {
    List<ChatConversation> selectConversationList(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size);

    ChatConversation selectConversationById(@Param("conversationId") Long conversationId);

    void insertConversation(ChatConversation conversation);

    int updateConversation(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("model") String model,
            @Param("systemPrompt") String systemPrompt);

    int softDeleteConversation(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    List<ChatMessage> selectMessages(@Param("conversationId") Long conversationId,
            @Param("afterMessageId") Long afterMessageId,
            @Param("limit") int limit);

    void insertMessage(ChatMessage message);

    ChatConversationSummary selectSummary(@Param("conversationId") Long conversationId);

    int upsertSummary(ChatConversationSummary summary);
}
