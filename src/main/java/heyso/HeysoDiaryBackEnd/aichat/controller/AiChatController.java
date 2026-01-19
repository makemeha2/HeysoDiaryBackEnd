package heyso.HeysoDiaryBackEnd.aichat.controller;

import heyso.HeysoDiaryBackEnd.aichat.dto.ChatAssistantReplyRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatAssistantReplyResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationCreateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationCreateResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationDetailResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationListResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatConversationUpdateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageCreateRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageCreateResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatMessageListResponse;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryUpsertRequest;
import heyso.HeysoDiaryBackEnd.aichat.dto.ChatSummaryResponse;
import heyso.HeysoDiaryBackEnd.aichat.service.AiChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/aichat")
public class AiChatController {
    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    // ====================== Conversation ======================

    @GetMapping("/conversations")
    public ChatConversationListResponse listConversations(@Valid @ModelAttribute ChatConversationListRequest request) {
        return aiChatService.listConversations(request);
    }

    @PostMapping("/conversations")
    public ResponseEntity<ChatConversationCreateResponse> createConversation(
            @Valid @RequestBody ChatConversationCreateRequest request) {
        ChatConversationCreateResponse response = aiChatService.createConversation(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversationDetailResponse> getConversation(
            @PathVariable @Positive Long conversationId,
            @RequestParam(value = "messageLimit", defaultValue = "50") @Min(1) @Max(200) int messageLimit) {
        return ResponseEntity.ok(aiChatService.getConversationDetail(conversationId, messageLimit));
    }

    @PostMapping("/conversations/{conversationId}/update")
    public ResponseEntity<Void> updateConversation(
            @PathVariable @Positive Long conversationId,
            @Valid @RequestBody ChatConversationUpdateRequest request) {
        aiChatService.updateConversation(conversationId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable @Positive Long conversationId) {
        aiChatService.softDeleteConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    // ====================== Messages ======================

    @GetMapping("/conversations/{conversationId}/messages")
    public ChatMessageListResponse listMessages(
            @PathVariable @Positive Long conversationId,
            @RequestParam(value = "afterMessageId", required = false) Long afterMessageId,
            @RequestParam(value = "limit", defaultValue = "50") @Min(1) @Max(200) int limit) {
        return aiChatService.listMessages(conversationId, afterMessageId, limit);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatMessageCreateResponse> createMessage(
            @PathVariable @Positive Long conversationId,
            @Valid @RequestBody ChatMessageCreateRequest request) {
        ChatMessageCreateResponse response = aiChatService.createMessage(conversationId, request);
        return ResponseEntity.status(201).body(response);
    }

    // ====================== Assistant Reply (OpenAI) ======================

    @PostMapping("/conversations/{conversationId}/assistant-reply")
    public ResponseEntity<ChatAssistantReplyResponse> createAssistantReply(
            @PathVariable @Positive Long conversationId,
            @Valid @RequestBody ChatAssistantReplyRequest request) {
        ChatAssistantReplyResponse response = aiChatService.createAssistantReply(conversationId, request);
        return ResponseEntity.status(201).body(response);
    }

    // ====================== Summary ======================

    @GetMapping("/conversations/{conversationId}/summary")
    public ResponseEntity<ChatSummaryResponse> getSummary(@PathVariable @Positive Long conversationId) {
        return ResponseEntity.ok(aiChatService.getSummary(conversationId));
    }

    @PutMapping("/conversations/{conversationId}/summary")
    public ResponseEntity<Void> upsertSummary(
            @PathVariable @Positive Long conversationId,
            @Valid @RequestBody ChatSummaryUpsertRequest request) {
        aiChatService.upsertSummary(conversationId, request);
        return ResponseEntity.ok().build();
    }
}
