package heyso.HeysoDiaryBackEnd.aichat.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.beans.factory.annotation.Value;
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
import heyso.HeysoDiaryBackEnd.aichat.mapper.AiChatDtoMapper;
import heyso.HeysoDiaryBackEnd.aichat.mapper.AiChatMapper;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversation;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatConversationSummary;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatMessage;
import heyso.HeysoDiaryBackEnd.aichat.model.ChatUsageLog;
import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallOptions;
import heyso.HeysoDiaryBackEnd.aichat.openai.AiCallResult;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient;
import heyso.HeysoDiaryBackEnd.aichat.openai.OpenAiClient.RoleMessage;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final AiChatMapper aiChatMapper;
    private final OpenAiClient openAiClient;
    private final AiChatDtoMapper dtoMapper;
    private final AiCallExecutor aiCallExecutor;

    private int SUMMARY_UPDATE_THRESHOLD = 10;
    private int SUMMARY_MAX_APPEND_MESSAGES = 10;

    @Value("${app.ai.context-message-limit:10}")
    private int contextMessageLimit;

    @Value("${app.ai.summary-prefix:Conversation summary:\n}")
    private String summaryPrefix;

    // 채팅방 목록을 가져온다.
    public ChatConversationListResponse listConversations(ChatConversationListRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        request.setUserId(user.getUserId());

        List<ChatConversation> conversations = aiChatMapper.selectConversationList(
                user.getUserId(),
                request.getOffset(),
                request.getSize());

        List<ChatConversationListItem> items = dtoMapper.toConversationListItems(conversations);

        return ChatConversationListResponse.of(items);
    }

    /// 채팅방을 생성한다.
    @Transactional
    public ChatConversationCreateResponse createConversation(ChatConversationCreateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        ChatConversation conversation = ChatConversation.builder()
                .userId(user.getUserId())
                .title(request.getTitle())
                .model(request.getModel())
                .systemPrompt(request.getSystemPrompt())
                .build();

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
        List<ChatMessageResponse> messageResponses = dtoMapper.toMessageResponses(messages);

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

        return ChatMessageListResponse.of(dtoMapper.toMessageResponses(messages));
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

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId).role(request.getRole())
                .content(request.getContent()).contentFormat(request.getContentFormat())
                .tokenCount(request.getTokenCount()).parentMessageId(request.getParentMessageId())
                .clientMessageId(request.getClientMessageId()).build();

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

        ChatConversationSummary summary = aiChatMapper.selectSummaryByUser(user.getUserId(), conversationId);
        if (summary == null) {
            // Return an empty object for convenience
            ChatConversationSummary empty = ChatConversationSummary.builder()
                    .conversationId(conversationId)
                    .summary("")
                    .summaryVersion(1)
                    .lastMessageId(null)
                    .updatedAt(null)
                    .build();
            return dtoMapper.toSummaryResponse(empty);
        }

        return dtoMapper.toSummaryResponse(summary);
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
        ChatMessage userMsg = ChatMessage.builder()
                .conversationId(conversationId).role("USER").content(request.getUserContent()).contentFormat("markdown")
                .parentMessageId(request.getParentMessageId()).clientMessageId(request.getUserClientMessageId())
                .build();

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

        ChatConversationSummary summary = aiChatMapper.selectSummaryByUser(user.getUserId(), conversationId);
        if (summary != null && summary.getSummary() != null && !summary.getSummary().isBlank()) {
            input.add(new RoleMessage("developer", summaryPrefix + summary.getSummary()));
        }

        List<ChatMessage> recentDesc = aiChatMapper.selectRecentMessages(conversationId, contextMessageLimit);
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

        AiCallResult result = null;
        try {
            result = aiCallExecutor.call(model, input, AiCallOptions.empty());
        } catch (Exception e) {
            // OpenAI 장애/네트워크 오류 시: USER 메시지는 남아있고 ASSISTANT는 없을 수 있음
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI request failed: " + e.getMessage());
        }

        String assistantText = result.content();
        String requestId = result.requestId();
        Integer promptTokens = result.promptTokens();
        Integer completionTokens = result.completionTokens();
        Integer totalTokens = result.totalTokens();

        if (assistantText == null || assistantText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI returned empty assistant content");
        }

        // 4) ASSISTANT 메시지 저장
        ChatMessage assistantMsg = ChatMessage.builder().conversationId(conversationId).role("ASSISTANT")
                .content(assistantText)
                .contentFormat(
                        request.getAssistantContentFormat() == null ? "markdown" : request.getAssistantContentFormat())
                .parentMessageId(userMsg.getMessageId()).clientMessageId(null).build();

        aiChatMapper.insertMessage(assistantMsg);

        // 5) usage log 저장(선택)
        ChatUsageLog log = ChatUsageLog.builder().userId(user.getUserId()).conversationId(conversationId)
                .requestId(requestId).model(model).promptTokens(promptTokens).completionTokens(completionTokens)
                .totalTokens(totalTokens).costUsd((BigDecimal) null).build();

        aiChatMapper.insertUsageLog(log);

        updateSummaryIfNeeded(user.getUserId(), conversationId, conversation.getModel());

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

    private void updateSummaryIfNeeded(Long userId, Long conversationId, String conversationModel) {

        // (1) 서비스 레벨에서도 소유권 체크 (이중 방어)
        ChatConversation conversation = aiChatMapper.selectConversationById(conversationId);
        if (conversation == null || conversation.getIsDeleted())
            return;
        if (!conversation.getUserId().equals(userId))
            return;

        // (2) summary 조회도 user 조건으로
        ChatConversationSummary summary = aiChatMapper.selectSummaryByUser(userId, conversationId);

        long lastMessageId = (summary == null || summary.getLastMessageId() == null)
                ? 0L
                : summary.getLastMessageId();

        // (3) count/select도 user 조건으로
        int newCount = aiChatMapper.countMessagesAfterByUser(userId, conversationId, lastMessageId);
        if (newCount < SUMMARY_UPDATE_THRESHOLD)
            return;

        List<ChatMessage> appended = aiChatMapper.selectMessagesAfterByUser(
                userId, conversationId, lastMessageId, SUMMARY_MAX_APPEND_MESSAGES);
        if (appended.isEmpty())
            return;

        long newLastMessageId = appended.get(appended.size() - 1).getMessageId();

        String prevSummary = (summary == null) ? "" : StringUtils.defaultString(summary.getSummary());
        String transcript = buildTranscript(appended);

        String summarizerInstruction = """
                You are updating a running conversation summary.
                - Write the updated summary in English.
                - Keep it concise but include stable facts, user preferences, commitments, plans, and unresolved tasks.
                - DO NOT include raw chat logs. DO NOT quote.
                - Preserve important constraints and decisions.
                Return ONLY the summary text.
                """;

        List<OpenAiClient.RoleMessage> input = new ArrayList<>();
        input.add(new OpenAiClient.RoleMessage("developer", summarizerInstruction));

        if (!prevSummary.isBlank()) {
            input.add(new OpenAiClient.RoleMessage("user", "Previous summary:\n" + prevSummary));
        }
        input.add(new OpenAiClient.RoleMessage("user", "New messages since last summary:\n" + transcript));

        CallResponseSpec updatedSummarySpec;
        try {
            updatedSummarySpec = openAiClient.createResponseSpec("gpt-4o-2024-08-06", input);
        } catch (Exception e) {
            // OpenAI 장애/네트워크 오류 시: USER 메시지는 남아있고 ASSISTANT는 없을 수 있음
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI request failed: " + e.getMessage());
        }

        // ChatResponse chatResponse = updatedSummarySpec.chatResponse();
        String updatedSummary = updatedSummarySpec.content();

        ChatConversationSummary upsert = ChatConversationSummary.builder()
                .conversationId(conversationId)
                .summary(updatedSummary.trim())
                .summaryVersion(summary == null ? 1 : (summary.getSummaryVersion() + 1))
                .lastMessageId(newLastMessageId)
                .build();

        // ✅ (4) upsert 실행 (여기 오기 전에 이미 소유권 검증 완료)
        aiChatMapper.upsertSummary(upsert);
    }

    private static String buildTranscript(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage m : messages) {
            String role = m.getRole(); // USER/ASSISTANT/SYSTEM
            // SYSTEM은 요약에 굳이 포함 안 해도 되는데, 필요하면 포함해도 됨
            if ("SYSTEM".equals(role))
                continue;

            sb.append(role).append(": ").append(StringUtils.defaultString(m.getContent())).append("\n");
        }
        return sb.toString();
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
