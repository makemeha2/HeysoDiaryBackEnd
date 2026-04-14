package heyso.HeysoDiaryBackEnd.comCd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.service.ComCdService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/admin/comCd")
public class AdminComCdController {
    private final ComCdService comCdService;

    public AdminComCdController(ComCdService comCdService) {
        this.comCdService = comCdService;
    }

    @GetMapping("/groups")
    public List<CommonCodeGroupResponse> getAdminGroupList(
            @RequestParam(defaultValue = "ACTIVE")
            @Pattern(regexp = "ACTIVE|INACTIVE|ALL", message = "status must be one of ACTIVE, INACTIVE, ALL") String status) {
        return comCdService.getAdminGroupList(status);
    }

    @GetMapping("/groups/{groupId}")
    public CommonCodeGroupResponse getAdminGroupDetail(
            @PathVariable @Size(max = 30) String groupId) {
        return comCdService.getAdminGroupDetail(groupId);
    }

    @PostMapping("/groups")
    public ResponseEntity<Void> createAdminGroup(@Valid @RequestBody CommonCodeGroupCreateRequest request) {
        comCdService.createAdminGroup(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/groups/{groupId}/update")
    public ResponseEntity<Void> updateAdminGroup(
            @PathVariable @Size(max = 30) String groupId,
            @Valid @RequestBody CommonCodeGroupUpdateRequest request) {
        comCdService.updateAdminGroup(groupId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/{groupId}/delete")
    public ResponseEntity<Void> deleteAdminGroup(
            @PathVariable @Size(max = 30) String groupId) {
        comCdService.deleteAdminGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/groups/{groupId}/codes")
    public List<CommonCodeResponse> getAdminCodeList(
            @PathVariable @Size(max = 30) String groupId,
            @RequestParam(defaultValue = "ACTIVE")
            @Pattern(regexp = "ACTIVE|INACTIVE|ALL", message = "status must be one of ACTIVE, INACTIVE, ALL") String status) {
        return comCdService.getAdminCodeList(groupId, status);
    }

    @GetMapping("/groups/{groupId}/codes/{codeId}")
    public CommonCodeResponse getAdminCodeDetail(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId) {
        return comCdService.getAdminCodeDetail(groupId, codeId);
    }

    @PostMapping("/groups/{groupId}/codes")
    public ResponseEntity<Void> createAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @Valid @RequestBody CommonCodeCreateRequest request) {
        comCdService.createAdminCode(groupId, request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/groups/{groupId}/codes/{codeId}/update")
    public ResponseEntity<Void> updateAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId,
            @Valid @RequestBody CommonCodeUpdateRequest request) {
        comCdService.updateAdminCode(groupId, codeId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/{groupId}/codes/{codeId}/delete")
    public ResponseEntity<Void> deleteAdminCode(
            @PathVariable @Size(max = 30) String groupId,
            @PathVariable @Size(max = 30) String codeId) {
        comCdService.deleteAdminCode(groupId, codeId);
        return ResponseEntity.ok().build();
    }
}
