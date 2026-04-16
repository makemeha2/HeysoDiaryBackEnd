package heyso.HeysoDiaryBackEnd.aiTemplate.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiRuntimeProfileMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiRuntimeProfileService {

    private final AdminAuthorizationService adminAuthorizationService;
    private final AiRuntimeProfileMapper aiRuntimeProfileMapper;

    public List<AiRuntimeProfileListResponse> getList(String status, String domainType) {
        adminAuthorizationService.requireAdminUser();

        String resolvedStatus = (status == null) ? "ALL" : status;
        List<AiRuntimeProfile> profiles = aiRuntimeProfileMapper.selectList(resolvedStatus, domainType);
        return profiles.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    public void create(AiRuntimeProfileCreateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiRuntimeProfile profile = AiRuntimeProfile.builder()
                .profileKey(request.getProfileKey())
                .profileName(request.getProfileName())
                .domainType(request.getDomainType())
                .provider(request.getProvider())
                .model(request.getModel())
                .temperature(request.getTemperature())
                .topP(request.getTopP())
                .maxTokens(request.getMaxTokens())
                .description(request.getDescription())
                .revisionNo(1)
                .isActive(1)
                .createdId(operatorId)
                .updatedId(operatorId)
                .build();

        aiRuntimeProfileMapper.insert(profile);
    }

    public void update(Long runtimeProfileId, AiRuntimeProfileUpdateRequest request) {
        Long operatorId = adminAuthorizationService.requireAdminUserId();

        AiRuntimeProfile existing = aiRuntimeProfileMapper.selectById(runtimeProfileId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RuntimeProfile not found: " + runtimeProfileId);
        }

        existing.setProfileName(request.getProfileName());
        existing.setDomainType(request.getDomainType());
        existing.setProvider(request.getProvider());
        existing.setModel(request.getModel());
        existing.setTemperature(request.getTemperature());
        existing.setTopP(request.getTopP());
        existing.setMaxTokens(request.getMaxTokens());
        existing.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        existing.setUpdatedId(operatorId);

        aiRuntimeProfileMapper.update(existing);
    }

    private AiRuntimeProfileListResponse toListResponse(AiRuntimeProfile p) {
        return AiRuntimeProfileListResponse.builder()
                .runtimeProfileId(p.getRuntimeProfileId())
                .profileKey(p.getProfileKey())
                .profileName(p.getProfileName())
                .domainType(p.getDomainType())
                .provider(p.getProvider())
                .model(p.getModel())
                .modelName(p.getModelName())
                .temperature(p.getTemperature())
                .topP(p.getTopP())
                .maxTokens(p.getMaxTokens())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
