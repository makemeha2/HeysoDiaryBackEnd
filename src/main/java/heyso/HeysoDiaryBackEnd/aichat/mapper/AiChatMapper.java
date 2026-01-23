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

        // 최근 N개 메시지(최신부터) 조회용
        List<ChatMessage> selectRecentMessages(@Param("conversationId") Long conversationId, @Param("limit") int limit);

        // 사용량 로그
        void insertUsageLog(heyso.HeysoDiaryBackEnd.aichat.model.ChatUsageLog usageLog);

        /*
         * ----------------------------------------------------------------------------
         * Summary 영역
         * ----------------------------------------------------------------------------
         */
        // ✅ userId 소유권 포함해서 summary 조회
        ChatConversationSummary selectSummaryByUser(@Param("userId") Long userId,
                        @Param("conversationId") Long conversationId);

        // ✅ userId 소유권 포함해서 "summary 이후 새 메시지 개수"
        int countMessagesAfterByUser(@Param("userId") Long userId,
                        @Param("conversationId") Long conversationId,
                        @Param("afterMessageId") Long afterMessageId);

        // ✅ userId 소유권 포함해서 "summary 이후 새 메시지 목록"
        List<ChatMessage> selectMessagesAfterByUser(@Param("userId") Long userId,
                        @Param("conversationId") Long conversationId,
                        @Param("afterMessageId") Long afterMessageId,
                        @Param("limit") int limit);

        // ✅ upsert summary 자체는 conversation_id로 가능하지만, 서비스에서 소유권 검증 후 호출
        void upsertSummary(ChatConversationSummary summary);
}
