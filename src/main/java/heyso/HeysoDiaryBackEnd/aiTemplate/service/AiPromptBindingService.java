package heyso.HeysoDiaryBackEnd.aiTemplate.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingDetailResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiPromptBindingUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptBindingMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiRuntimeProfileMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptBinding;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplate;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiPromptBindingService {

    private final AdminAuthorizationService adminAuthorizationService;
    private final AiPromptBindingMapper aiPromptBindingMapper;
    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final AiRuntimeProfileMapper aiRuntimeProfileMapper;

    public List<AiPromptBindingListResponse> getList(String status) {
        adminAuthorizationService.requireAdminUser();

        String resolvedStatus = (status == null) ? "ALL" : status;
        List<AiPromptBinding> bindings = aiPromptBindingMapper.selectList(resolvedStatus);
        return bindings.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    public AiPromptBindingDetailResponse getDetail(Long bindingId) {
        adminAuthorizationService.requireAdminUser();

        AiPromptBinding binding = aiPromptBindingMapper.selectById(bindingId);
        if (binding == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Binding not found: " + bindingId);
        }

        AiPromptTemplate systemTemplate = aiPromptTemplateMapper.selectById(binding.getSystemTemplateId());
        AiPromptTemplate userTemplate = aiPromptTemplateMapper.selectById(binding.getUserTemplateId());
        AiRuntimeProfile profile = aiRuntimeProfileMapper.selectById(binding.getRuntimeProfileId());

        return AiPromptBindingDetailResponse.builder()
                .bindingId(binding.getBindingId())
                .bindingName(binding.getBindingName())
                .domainType(binding.getDomainType())
                .featureKey(binding.getFeatureKey())
                .systemTemplateId(binding.getSystemTemplateId())
                .systemTemplateName(systemTemplate != null ? systemTemplate.getTemplateName() : null)
                .userTemplateId(binding.getUserTemplateId())
                .userTemplateName(userTemplate != null ? userTemplate.getTemplateName() : null)
                .runtimeProfileId(binding.getRuntimeProfileId())
                .profileName(profile != null ? profile.getProfileName() : null)
                .description(binding.getDescription())
                .isActive(binding.getIsActive())
                .createdAt(binding.getCreatedAt())
                .createdId(binding.getCreatedId())
                .updatedAt(binding.getUpdatedAt())
                .updatedId(binding.getUpdatedId())
                .build();
    }

    public void create(AiPromptBindingCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiPromptBinding binding = AiPromptBinding.builder()
                .bindingName(request.getBindingName())
                .domainType(request.getDomainType())
                .featureKey(request.getFeatureKey())
                .systemTemplateId(request.getSystemTemplateId())
                .userTemplateId(request.getUserTemplateId())
                .runtimeProfileId(request.getRuntimeProfileId())
                .description(request.getDescription())
                .isActive(1)
                .createdId(operatorId)
                .updatedId(operatorId)
                .build();

        aiPromptBindingMapper.insert(binding);
    }

    public void update(Long bindingId, AiPromptBindingUpdateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiPromptBinding existing = aiPromptBindingMapper.selectById(bindingId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Binding not found: " + bindingId);
        }

        existing.setBindingName(request.getBindingName());
        existing.setDomainType(request.getDomainType());
        existing.setSystemTemplateId(request.getSystemTemplateId());
        existing.setUserTemplateId(request.getUserTemplateId());
        existing.setRuntimeProfileId(request.getRuntimeProfileId());
        existing.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedId(operatorId);

        aiPromptBindingMapper.update(existing);
    }

    private AiPromptBindingListResponse toListResponse(AiPromptBinding b) {
        return AiPromptBindingListResponse.builder()
                .bindingId(b.getBindingId())
                .bindingName(b.getBindingName())
                .domainType(b.getDomainType())
                .featureKey(b.getFeatureKey())
                .systemTemplateId(b.getSystemTemplateId())
                .userTemplateId(b.getUserTemplateId())
                .runtimeProfileId(b.getRuntimeProfileId())
                .isActive(b.getIsActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
