package heyso.HeysoDiaryBackEnd.aiTemplate.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingDetailResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateDetailResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateRelCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateRelResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiTemplatePreviewRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiTemplatePreviewResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.service.AiPromptBindingService;
import heyso.HeysoDiaryBackEnd.aiTemplate.service.AiPromptTemplateService;
import heyso.HeysoDiaryBackEnd.aiTemplate.service.AiRuntimeProfileService;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/ai-template")
@RequiredArgsConstructor
public class AdminAiTemplateController {

    private final AiPromptTemplateService aiPromptTemplateService;
    private final AiRuntimeProfileService aiRuntimeProfileService;
    private final AiPromptBindingService aiPromptBindingService;
    private final AdminAuthorizationService adminAuthorizationService;

    // =========================================================================
    // Template
    // =========================================================================

    @GetMapping("/templates")
    public List<AiPromptTemplateListResponse> getTemplateList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String domainType) {
        return aiPromptTemplateService.getList(status, templateType, domainType);
    }

    @GetMapping("/templates/{templateId}")
    public AiPromptTemplateDetailResponse getTemplateDetail(@PathVariable Long templateId) {
        return aiPromptTemplateService.getDetail(templateId);
    }

    @PostMapping("/templates")
    public ResponseEntity<Void> createTemplate(@Valid @RequestBody AiPromptTemplateCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();
        aiPromptTemplateService.create(request, operatorId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/templates/{templateId}/update")
    public ResponseEntity<Void> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody AiPromptTemplateUpdateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();
        aiPromptTemplateService.update(templateId, request, operatorId);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // Template Relations
    // =========================================================================

    @GetMapping("/templates/{templateId}/relations")
    public List<AiPromptTemplateRelResponse> getTemplateRelations(@PathVariable Long templateId) {
        return aiPromptTemplateService.getRelations(templateId);
    }

    @PostMapping("/templates/{templateId}/relations")
    public ResponseEntity<Void> addTemplateRelation(
            @PathVariable Long templateId,
            @Valid @RequestBody AiPromptTemplateRelCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();
        aiPromptTemplateService.addRelation(templateId, request, operatorId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/templates/{templateId}/relations/{relId}/delete")
    public ResponseEntity<Void> deleteTemplateRelation(
            @PathVariable Long templateId,
            @PathVariable Long relId) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();
        aiPromptTemplateService.deleteRelation(templateId, relId, operatorId);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // Preview
    // =========================================================================

    @PostMapping("/templates/{templateId}/preview")
    public AiTemplatePreviewResponse previewTemplate(
            @PathVariable Long templateId,
            @RequestBody(required = false) AiTemplatePreviewRequest request) {
        Map<String, String> variables = (request != null) ? request.getVariables() : null;
        return aiPromptTemplateService.preview(templateId, variables);
    }

    // =========================================================================
    // Runtime Profile
    // =========================================================================

    @GetMapping("/runtime-profiles")
    public List<AiRuntimeProfileListResponse> getRuntimeProfileList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String domainType) {
        return aiRuntimeProfileService.getList(status, domainType);
    }

    @PostMapping("/runtime-profiles")
    public ResponseEntity<Void> createRuntimeProfile(@Valid @RequestBody AiRuntimeProfileCreateRequest request) {
        aiRuntimeProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/runtime-profiles/{runtimeProfileId}/update")
    public ResponseEntity<Void> updateRuntimeProfile(
            @PathVariable Long runtimeProfileId,
            @Valid @RequestBody AiRuntimeProfileUpdateRequest request) {
        aiRuntimeProfileService.update(runtimeProfileId, request);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // Binding
    // =========================================================================

    @GetMapping("/bindings")
    public List<AiPromptBindingListResponse> getBindingList(
            @RequestParam(required = false) String status) {
        return aiPromptBindingService.getList(status);
    }

    @GetMapping("/bindings/{bindingId}")
    public AiPromptBindingDetailResponse getBindingDetail(@PathVariable Long bindingId) {
        return aiPromptBindingService.getDetail(bindingId);
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody AiPromptBindingCreateRequest request) {
        aiPromptBindingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/bindings/{bindingId}/update")
    public ResponseEntity<Void> updateBinding(
            @PathVariable Long bindingId,
            @Valid @RequestBody AiPromptBindingUpdateRequest request) {
        aiPromptBindingService.update(bindingId, request);
        return ResponseEntity.ok().build();
    }
}
