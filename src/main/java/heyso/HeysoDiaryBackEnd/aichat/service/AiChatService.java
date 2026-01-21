package heyso.HeysoDiaryBackEnd.aichat.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aichat.dto.ChatAssistantReplyRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatAssistantReplyResponse;
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
import heyso.HeysoDiaryBackEnd.aichat.model.ChatUsageLog;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiProperties;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;

@Service
public class AiChatService {
    private final AiChatMapper aiChatMapper;
    private final OpenAiClient openAiClient;
    private final OpenAiProperties openAiProperties;

    public AiChatService(AiChatMapper aiChatMapper, OpenAiClient openAiClient, OpenAiProperties openAiProperties) {
        this.aiChatMapper = aiChatMapper;
        this.openAiClient = openAiClient;
        this.openAiProperties = openAiProperties;
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

    /// OpenAI Chat봇의 응답을 가져온다.
    @Transactional
    public ChatAssistantReplyResponse createAssistantReply(Long conversationId, ChatAssistantReplyRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this conversation");
        }

        // 1) USER 메시지 저장
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("USER");
        userMsg.setContent(request.getUserContent());
        userMsg.setContentFormat("markdown");
        userMsg.setParentMessageId(request.getParentMessageId());
        userMsg.setClientMessageId(request.getUserClientMessageId());

        try {
            aiChatMapper.insertMessage(userMsg);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate clientMessageId in this conversation");
        }

        // 2) OpenAI input 구성
        // - developer: conversation.systemPrompt
        // - developer: summary(있으면)
        // - messages: 최근 N개 메시지(최신->역순 정렬해서 시간순으로)
        List<RoleMessage> input = new ArrayList<>();

        if (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().isBlank()) {
            input.add(new RoleMessage("developer", conversation.getSystemPrompt()));
        }

        ChatConversationSummary summary = aiChatMapper.selectSummary(conversationId);
        if (summary != null && summary.getSummary() != null && !summary.getSummary().isBlank()) {
            input.add(new RoleMessage("developer", openAiProperties.getSummaryPrefix() + summary.getSummary()));
        }

        List<ChatMessage> recentDesc = aiChatMapper.selectRecentMessages(conversationId,
                openAiProperties.getContextMessageLimit());
        Collections.reverse(recentDesc); // 시간순 정렬(오래된 -> 최신)

        for (ChatMessage m : recentDesc) {
            String role = mapRoleForOpenAi(m.getRole());
            if (role == null)
                continue;

            // 방금 저장한 USER 메시지도 포함되도록 recentDesc에 들어있음
            input.add(new RoleMessage(role, m.getContent()));
        }

        // 3) OpenAI 호출
        String model = (conversation.getModel() == null || conversation.getModel().isBlank())
                ? "gpt-4o-mini"
                : conversation.getModel();

        CallResponseSpec responseSpec;
        try {
            responseSpec = openAiClient.createResponseText(model, input);
        } catch (Exception e) {
            // OpenAI 장애/네트워크 오류 시: USER 메시지는 남아있고 ASSISTANT는 없을 수 있음
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI request failed: " + e.getMessage());
        }

        ChatResponse chatResponse = responseSpec.chatResponse();
        String assistantText = responseSpec.content();

        String requestId = null;
        Integer promptTokens = null;
        Integer completionTokens = null;
        Integer totalTokens = null;

        if (chatResponse != null && chatResponse.getMetadata() != null) {
            String metadataId = chatResponse.getMetadata().getId();
            if (metadataId != null && !metadataId.isBlank()) {
                requestId = metadataId;
            }

            Usage usage = chatResponse.getMetadata().getUsage();
            if (usage != null && !(usage instanceof EmptyUsage)) {
                promptTokens = usage.getPromptTokens();
                completionTokens = usage.getCompletionTokens();
                totalTokens = usage.getTotalTokens();
            }
        }

        if (assistantText == null || assistantText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned empty assistant content");
        }

        // 4) ASSISTANT 메시지 저장
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setConversationId(conversationId);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(assistantText);
        assistantMsg.setContentFormat(
                request.getAssistantContentFormat() == null ? "markdown" : request.getAssistantContentFormat());
        // token_count는 우리 DB 컬럼이고, OpenAI usage와 일치하지 않을 수 있어 null 유지
        assistantMsg.setParentMessageId(userMsg.getMessageId());
        assistantMsg.setClientMessageId(null);

        aiChatMapper.insertMessage(assistantMsg);

        // 5) usage log 저장(선택)
        ChatUsageLog log = new ChatUsageLog();
        log.setUserId(user.getUserId());
        log.setConversationId(conversationId);
        log.setRequestId(requestId);
        log.setModel(model);
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(totalTokens);
        // costUsd는 요금표 기반 계산이 필요해서 일단 null (원하면 여기서 계산 로직 추가)
        log.setCostUsd((BigDecimal) null);

        aiChatMapper.insertUsageLog(log);

        return ChatAssistantReplyResponse.builder()
                .userMessageId(userMsg.getMessageId())
                .assistantMessageId(assistantMsg.getMessageId())
                .model(model)
                .requestId(requestId)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .assistantContent(assistantText)
                .build();
    }

    private static String mapRoleForOpenAi(String dbRole) {
        if (dbRole == null)
            return null;

        return switch (dbRole) {
            // DB의 SYSTEM은 OpenAI의 developer로 승격
            case "SYSTEM" -> "developer";
            case "USER" -> "user";
            case "ASSISTANT" -> "assistant";
            // TOOL은 이번 버전에서는 제외(추후 tool-calling 붙이면 확장)
            default -> null;
        };
    }
}
