package heyso.HeysoDiaryBackEnd.comCd.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.service.ComCdService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/comCd")
public class ComCdController {
    private final ComCdService comCdService;

    public ComCdController(ComCdService comCdService) {
        this.comCdService = comCdService;
    }

    /* ------------------------------ 관리자: 그룹 CRUD ------------------------------ */

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @GetMapping("/admin/groups")
    public List<CommonCodeGroupResponse> getAdminGroupList() {
        return comCdService.getAdminGroupList();
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @GetMapping("/admin/groups/{groupId}")
    public CommonCodeGroupResponse getAdminGroupDetail(
            @PathVariable @Size(max = 30) String groupId) {
        return comCdService.getAdminGroupDetail(groupId);
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups")
    public ResponseEntity<Void> createAdminGroup(@Valid @RequestBody CommonCodeGroupCreateRequest request) {
        comCdService.createAdminGroup(request);
        return ResponseEntity.status(201).build();
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups/{groupId}/update")
    public ResponseEntity<Void> updateAdminGroup(
            @PathVariable @Size(max = 30) String groupId,
            @Valid @RequestBody CommonCodeGroupUpdateRequest request) {
        comCdService.updateAdminGroup(groupId, request);
        return ResponseEntity.ok().build();
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups/{groupId}/delete")
    public ResponseEntity<Void> deleteAdminGroup(
            @PathVariable @Size(max = 30) String groupId) {
        comCdService.deleteAdminGroup(groupId);
        return ResponseEntity.ok().build();
    }

    /* ------------------------------ 관리자: 코드 CRUD ------------------------------ */

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @GetMapping("/admin/groups/{groupId}/codes")
    public List<CommonCodeResponse> getAdminCodeList(
            @PathVariable @Size(max = 30) String groupId) {
        return comCdService.getAdminCodeList(groupId);
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @GetMapping("/admin/groups/{groupId}/codes/{codeId}")
    public CommonCodeResponse getAdminCodeDetail(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId) {
        return comCdService.getAdminCodeDetail(groupId, codeId);
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups/{groupId}/codes")
    public ResponseEntity<Void> createAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @Valid @RequestBody CommonCodeCreateRequest request) {
        comCdService.createAdminCode(groupId, request);
        return ResponseEntity.status(201).build();
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups/{groupId}/codes/{codeId}/update")
    public ResponseEntity<Void> updateAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId,
            @Valid @RequestBody CommonCodeUpdateRequest request) {
        comCdService.updateAdminCode(groupId, codeId, request);
        return ResponseEntity.ok().build();
    }

    @ConditionalOnProperty(name = "feature.comCdAdmin.enabled", havingValue = "true", matchIfMissing = false)
    @PostMapping("/admin/groups/{groupId}/codes/{codeId}/delete")
    public ResponseEntity<Void> deleteAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId) {
        comCdService.deleteAdminCode(groupId, codeId);
        return ResponseEntity.ok().build();
    }

    /*
     * ------------------------------ 사용자: 활성 코드 조회 ------------------------------
     */

    @GetMapping("/groups")
    public List<CommonCodeGroupResponse> getActiveGroupList() {
        return comCdService.getActiveGroupList();
    }

    @GetMapping("/groups/{groupId}/codes")
    public List<CommonCodeResponse> getActiveCodeListByGroupId(
            @PathVariable @Size(max = 30) String groupId) {
        return comCdService.getActiveCodeListByGroupId(groupId);
    }
}
