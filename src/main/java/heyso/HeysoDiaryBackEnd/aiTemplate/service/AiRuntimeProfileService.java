package heyso.HeysoDiaryBackEnd.aiTemplate.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileCreateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileListResponse;
import heyso.HeysoDiaryBackEnd.aiTemplate.dto.AiRuntimeProfileUpdateRequest;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiRuntimeProfileMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiRuntimeProfileService {

    private static final String ADMIN_SCOPE_AUTHORITY = "SCOPE_admin";

    private final AiRuntimeProfileMapper aiRuntimeProfileMapper;

    public List<AiRuntimeProfileListResponse> getList(String status, String domainType) {
        String resolvedStatus = (status == null) ? "ALL" : status;
        List<AiRuntimeProfile> profiles = aiRuntimeProfileMapper.selectList(resolvedStatus, domainType);
        return profiles.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    public void create(AiRuntimeProfileCreateRequest request) {
        Long operatorId = requireAdminUserId();

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
        Long operatorId = requireAdminUserId();

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
        existing.setUpdatedId(operatorId);

        aiRuntimeProfileMapper.update(existing);
    }

    public void softDelete(Long runtimeProfileId) {
        Long operatorId = requireAdminUserId();

        AiRuntimeProfile existing = aiRuntimeProfileMapper.selectById(runtimeProfileId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RuntimeProfile not found: " + runtimeProfileId);
        }
        aiRuntimeProfileMapper.updateIsActive(runtimeProfileId, 0, operatorId);
    }

    private Long requireAdminUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminScope = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(a -> ADMIN_SCOPE_AUTHORITY.equals(a.getAuthority()));
        if (!hasAdminScope) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin scope required");
        }
        User user = SecurityUtils.getCurrentUserOrThrow();
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user.getUserId();
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
