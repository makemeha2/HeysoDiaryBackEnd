package heyso.HeysoDiaryBackEnd.aichat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationCreateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationCreateResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationDetailResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListItem;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationUpdateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageCreateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageCreateResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageListResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryUpsertRequest;
import heyso.HeysoDiaryBackEnd.aichat.mapper.AiChatMapper;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversationSummary;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatMessage;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;

@Service
public class AiChatService {
    private final AiChatMapper aiChatMapper;

    public AiChatService(AiChatMapper aiChatMapper) {
        this.aiChatMapper = aiChatMapper;
    }

    // 채팅방 목록을 가져온다.
    public ChatConversationListResponse listConversations(ChatConversationListRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        request.setUserId(user.getUserId());

        List<ChatConversation> conversations = aiChatMapper.selectConversationList(
                user.getUserId(),
                request.getOffset(),
                request.getSize());

        List<ChatConversationListItem> items = conversations.stream()
                .map(ChatConversationListItem::from)
                .collect(Collectors.toList());

        return ChatConversationListResponse.of(items);
    }

    /// 채팅방을 생성한다.
    @Transactional
    public ChatConversationCreateResponse createConversation(ChatConversationCreateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = new ChatConversation();
        conversation.setUserId(user.getUserId());
        conversation.setTitle(request.getTitle());
        conversation.setModel(request.getModel());
        conversation.setSystemPrompt(request.getSystemPrompt());

        aiChatMapper.insertConversation(conversation);
        return ChatConversationCreateResponse.of(conversation.getConversationId());
    }

    /// 채팅방의 상세 정보를 가져온다.
    public ChatConversationDetailResponse getConversationDetail(Long conversationId, int messageLimit) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this conversation");
        }

        List<ChatMessage> messages = aiChatMapper.selectMessages(conversationId, null, messageLimit);
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());

        return ChatConversationDetailResponse.of(conversation, messageResponses);
    }

    /// 채팅방을 수정한다.(채팅방명, 모델종류, 지시 등)
    @Transactional
    public void updateConversation(Long conversationId, ChatConversationUpdateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        // Ensure ownership (and existence)
        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update this conversation");
        }

        int updated = aiChatMapper.updateConversation(
                conversationId,
                user.getUserId(),
                request.getTitle() != null ? request.getTitle() : conversation.getTitle(),
                request.getModel(),
                request.getSystemPrompt());
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to update conversation");
        }
    }

    /// 삭제
    @Transactional
    public void softDeleteConversation(Long conversationId) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        int updated = aiChatMapper.softDeleteConversation(conversationId, user.getUserId());
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
    }

    /// 채팅방의 대화목록을 가져온다.
    public ChatMessageListResponse listMessages(Long conversationId, Long afterMessageId, int limit) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this conversation");
        }

        List<ChatMessage> messages = aiChatMapper.selectMessages(conversationId, afterMessageId, limit);
        return ChatMessageListResponse
                .of(messages.stream().map(ChatMessageResponse::from).collect(Collectors.toList()));
    }

    /// 대화를 추가한다.
    @Transactional
    public ChatMessageCreateResponse createMessage(Long conversationId, ChatMessageCreateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot add messages to this conversation");
        }

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(request.getRole());
        message.setContent(request.getContent());
        message.setContentFormat(request.getContentFormat());
        message.setTokenCount(request.getTokenCount());
        message.setParentMessageId(request.getParentMessageId());
        message.setClientMessageId(request.getClientMessageId());

        try {
            aiChatMapper.insertMessage(message);
        } catch (DuplicateKeyException e) {
            // uk_tb_chat_msg_client (conversation_id, client_message_id)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate clientMessageId in this conversation");
        }

        return ChatMessageCreateResponse.of(message.getMessageId());
    }

    /// 채팅방 대화내용의 Summary 를 가져온다.
    public ChatSummaryResponse getSummary(Long conversationId) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this conversation");
        }

        ChatConversationSummary summary = aiChatMapper.selectSummary(conversationId);
        if (summary == null) {
            // Return an empty object for convenience
            ChatConversationSummary empty = new ChatConversationSummary();
            empty.setConversationId(conversationId);
            empty.setSummary("");
            empty.setSummaryVersion(1);
            empty.setLastMessageId(null);
            empty.setUpdatedAt(null);
            return ChatSummaryResponse.from(empty);
        }

        return ChatSummaryResponse.from(summary);
    }

    /// 채팅방 대화내용의 Summary 를 갱신한다.
    @Transactional
    public void upsertSummary(Long conversationId, ChatSummaryUpsertRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this conversation");
        }

        ChatConversationSummary summary = new ChatConversationSummary();
        summary.setConversationId(conversationId);
        summary.setSummary(request.getSummary());
        summary.setSummaryVersion(request.getSummaryVersion());
        summary.setLastMessageId(request.getLastMessageId());

        aiChatMapper.upsertSummary(summary);
    }
}
