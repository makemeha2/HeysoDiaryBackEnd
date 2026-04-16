package heyso.HeysoDiaryBackEnd.aiTemplate.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.ai.support.AiTemplateRenderer;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateDetailResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateRelCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateRelResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptTemplateUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiTemplatePreviewResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateRelMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplate;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplateRel;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiPromptTemplateService {

    private final AdminAuthorizationService adminAuthorizationService;
    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final AiPromptTemplateRelMapper aiPromptTemplateRelMapper;
    private final AiTemplateRenderer aiTemplateRenderer;

    public List<AiPromptTemplateListResponse> getList(String status, String templateType, String domainType) {
        adminAuthorizationService.requireAdminUser();

        String resolvedStatus = (status == null) ? "ALL" : status;
        List<AiPromptTemplate> templates = aiPromptTemplateMapper.selectList(resolvedStatus, templateType, domainType);
        return templates.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    public AiPromptTemplateDetailResponse getDetail(Long templateId) {
        adminAuthorizationService.requireAdminUser();

        AiPromptTemplate template = aiPromptTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId);
        }
        List<AiPromptTemplateRelResponse> relations = getRelations(templateId);
        return toDetailResponse(template, relations);
    }

    public void create(AiPromptTemplateCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiPromptTemplate existing = aiPromptTemplateMapper.selectByKey(request.getTemplateKey());
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Template key already exists: " + request.getTemplateKey());
        }

        AiPromptTemplate template = AiPromptTemplate.builder()
                .templateKey(request.getTemplateKey())
                .templateName(request.getTemplateName())
                .domainType(request.getDomainType())
                .featureKey(request.getFeatureKey())
                .templateRole(request.getTemplateRole())
                .templateType(request.getTemplateType())
                .content(request.getContent())
                .variablesSchemaJson(request.getVariablesSchemaJson())
                .description(request.getDescription())
                .revisionNo(1)
                .isActive(1)
                .createdId(operatorId)
                .updatedId(operatorId)
                .build();

        aiPromptTemplateMapper.insert(template);
    }

    public void update(Long templateId, AiPromptTemplateUpdateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiPromptTemplate existing = aiPromptTemplateMapper.selectById(templateId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId);
        }

        existing.setTemplateName(request.getTemplateName());
        existing.setDomainType(request.getDomainType());
        existing.setFeatureKey(request.getFeatureKey());
        existing.setTemplateRole(request.getTemplateRole());
        existing.setTemplateType(request.getTemplateType());
        existing.setContent(request.getContent());
        existing.setVariablesSchemaJson(request.getVariablesSchemaJson());
        existing.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedId(operatorId);

        aiPromptTemplateMapper.update(existing);
    }

    public List<AiPromptTemplateRelResponse> getRelations(Long parentTemplateId) {
        adminAuthorizationService.requireAdminUser();

        List<AiPromptTemplateRel> rels = aiPromptTemplateRelMapper.selectByParentId(parentTemplateId);
        return rels.stream()
                .map(rel -> {
                    AiPromptTemplate child = aiPromptTemplateMapper.selectById(rel.getChildTemplateId());
                    return AiPromptTemplateRelResponse.builder()
                            .relId(rel.getRelId())
                            .parentTemplateId(rel.getParentTemplateId())
                            .childTemplateId(rel.getChildTemplateId())
                            .childTemplateKey(child != null ? child.getTemplateKey() : null)
                            .childTemplateName(child != null ? child.getTemplateName() : null)
                            .mergeType(rel.getMergeType())
                            .sortSeq(rel.getSortSeq())
                            .isActive(rel.getIsActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void addRelation(Long parentTemplateId, AiPromptTemplateRelCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiPromptTemplate parent = aiPromptTemplateMapper.selectById(parentTemplateId);
        if (parent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent template not found: " + parentTemplateId);
        }
        if (!"ROOT".equals(parent.getTemplateType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent template must be of type ROOT");
        }

        AiPromptTemplate child = aiPromptTemplateMapper.selectById(request.getChildTemplateId());
        if (child == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Child template not found: " + request.getChildTemplateId());
        }
        if (!"FRAGMENT".equals(child.getTemplateType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Child template must be of type FRAGMENT");
        }

        AiPromptTemplateRel rel = AiPromptTemplateRel.builder()
                .parentTemplateId(parentTemplateId)
                .childTemplateId(request.getChildTemplateId())
                .mergeType(request.getMergeType())
                .sortSeq(request.getSortSeq())
                .isActive(1)
                .createdId(operatorId)
                .updatedId(operatorId)
                .build();

        aiPromptTemplateRelMapper.insert(rel);
    }

    public void deleteRelation(Long parentTemplateId, Long relId) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        aiPromptTemplateRelMapper.updateIsActive(relId, 0, operatorId);
    }

    public AiTemplatePreviewResponse preview(Long templateId, Map<String, String> variables) {
        adminAuthorizationService.requireAdminUser();

        AiPromptTemplate template = aiPromptTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId);
        }
        String rendered = aiTemplateRenderer.render(templateId, variables);
        return AiTemplatePreviewResponse.builder()
                .templateId(template.getTemplateId())
                .templateKey(template.getTemplateKey())
                .renderedContent(rendered)
                .build();
    }

    private AiPromptTemplateListResponse toListResponse(AiPromptTemplate t) {
        return AiPromptTemplateListResponse.builder()
                .templateId(t.getTemplateId())
                .templateKey(t.getTemplateKey())
                .templateName(t.getTemplateName())
                .domainType(t.getDomainType())
                .featureKey(t.getFeatureKey())
                .templateRole(t.getTemplateRole())
                .templateType(t.getTemplateType())
                .revisionNo(t.getRevisionNo())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private AiPromptTemplateDetailResponse toDetailResponse(AiPromptTemplate t,
            List<AiPromptTemplateRelResponse> relations) {
        return AiPromptTemplateDetailResponse.builder()
                .templateId(t.getTemplateId())
                .templateKey(t.getTemplateKey())
                .templateName(t.getTemplateName())
                .domainType(t.getDomainType())
                .featureKey(t.getFeatureKey())
                .templateRole(t.getTemplateRole())
                .templateType(t.getTemplateType())
                .content(t.getContent())
                .variablesSchemaJson(t.getVariablesSchemaJson())
                .description(t.getDescription())
                .revisionNo(t.getRevisionNo())
                .isActive(t.getIsActive())
                .createdAt(t.getCreatedAt())
                .createdId(t.getCreatedId())
                .updatedAt(t.getUpdatedAt())
                .updatedId(t.getUpdatedId())
                .relations(relations)
                .build();
    }
}
