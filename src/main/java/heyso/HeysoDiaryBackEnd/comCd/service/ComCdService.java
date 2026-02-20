package heyso.HeysoDiaryBackEnd.comCd.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupCreateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeUpdateRequest;
import heyso.HeysoDiaryBackEnd.comCd.mapper.CommonCodeMapper;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCodeGroup;
import heyso.HeysoDiaryBackEnd.user.model.User;

@Service
public class ComCdService {
    private final CommonCodeMapper commonCodeMapper;

    public ComCdService(CommonCodeMapper commonCodeMapper) {
        this.commonCodeMapper = commonCodeMapper;
    }

    public List<CommonCodeGroupResponse> getAdminGroupList() {
        requireAdminUser();
        return commonCodeMapper.selectCommonCodeGroupListForAdmin()
                .stream()
                .map(CommonCodeGroupResponse::from)
                .toList();
    }

    public CommonCodeGroupResponse getAdminGroupDetail(String groupId) {
        requireAdminUser();
        return CommonCodeGroupResponse.from(getGroupOrThrow(groupId));
    }

    @Transactional
    public void createAdminGroup(CommonCodeGroupCreateRequest request) {
        User user = requireAdminUser();

        CommonCodeGroup existing = commonCodeMapper.selectCommonCodeGroupById(request.getGroupId());
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Common code group already exists");
        }

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupId(request.getGroupId())
                .groupName(request.getGroupName())
                .isActive(request.getIsActive())
                .createdId(user.getUserId())
                .updatedId(user.getUserId())
                .build();

        commonCodeMapper.insertCommonCodeGroup(group);
    }

    @Transactional
    public void updateAdminGroup(String groupId, CommonCodeGroupUpdateRequest request) {
        User user = requireAdminUser();
        getGroupOrThrow(groupId);

        CommonCodeGroup group = CommonCodeGroup.builder()
                .groupId(groupId)
                .groupName(request.getGroupName())
                .isActive(request.getIsActive())
                .updatedId(user.getUserId())
                .build();

        commonCodeMapper.updateCommonCodeGroup(group);
    }

    @Transactional
    public void deleteAdminGroup(String groupId) {
        User user = requireAdminUser();
        getGroupOrThrow(groupId);
        commonCodeMapper.deleteCommonCodeGroup(groupId, user.getUserId());
    }

    public List<CommonCodeResponse> getAdminCodeList(String groupId) {
        requireAdminUser();
        getGroupOrThrow(groupId);
        return commonCodeMapper.selectCommonCodeListForAdmin(groupId)
                .stream()
                .map(CommonCodeResponse::from)
                .toList();
    }

    public CommonCodeResponse getAdminCodeDetail(String groupId, String codeId) {
        requireAdminUser();
        CommonCode commonCode = getCodeOrThrow(groupId, codeId);
        return CommonCodeResponse.from(commonCode);
    }

    @Transactional
    public void createAdminCode(String groupId, CommonCodeCreateRequest request) {
        User user = requireAdminUser();
        getGroupOrThrow(groupId);

        CommonCode existing = commonCodeMapper.selectCommonCodeById(groupId, request.getCodeId());
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Common code already exists");
        }

        CommonCode commonCode = CommonCode.builder()
                .groupId(groupId)
                .codeId(request.getCodeId())
                .codeName(request.getCodeName())
                .isActive(request.getIsActive())
                .extraInfo1(request.getExtraInfo1())
                .extraInfo2(request.getExtraInfo2())
                .sortSeq(request.getSortSeq())
                .createdId(user.getUserId())
                .updatedId(user.getUserId())
                .build();

        commonCodeMapper.insertCommonCode(commonCode);
    }

    @Transactional
    public void updateAdminCode(String groupId, String codeId, CommonCodeUpdateRequest request) {
        User user = requireAdminUser();
        getCodeOrThrow(groupId, codeId);

        CommonCode commonCode = CommonCode.builder()
                .groupId(groupId)
                .codeId(codeId)
                .codeName(request.getCodeName())
                .isActive(request.getIsActive())
                .extraInfo1(request.getExtraInfo1())
                .extraInfo2(request.getExtraInfo2())
                .sortSeq(request.getSortSeq())
                .updatedId(user.getUserId())
                .build();

        commonCodeMapper.updateCommonCode(commonCode);
    }

    @Transactional
    public void deleteAdminCode(String groupId, String codeId) {
        User user = requireAdminUser();
        getCodeOrThrow(groupId, codeId);
        commonCodeMapper.deleteCommonCode(groupId, codeId, user.getUserId());
    }

    public List<CommonCodeGroupResponse> getActiveGroupList() {
        SecurityUtils.getCurrentUserOrThrow();
        return commonCodeMapper.selectActiveCommonCodeGroupList()
                .stream()
                .map(CommonCodeGroupResponse::from)
                .toList();
    }

    public List<CommonCodeResponse> getActiveCodeListByGroupId(String groupId) {
        SecurityUtils.getCurrentUserOrThrow();
        return commonCodeMapper.selectActiveCommonCodeListByGroupId(groupId)
                .stream()
                .map(CommonCodeResponse::from)
                .toList();
    }

    private CommonCodeGroup getGroupOrThrow(String groupId) {
        CommonCodeGroup group = commonCodeMapper.selectCommonCodeGroupById(groupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Common code group not found");
        }
        return group;
    }

    private CommonCode getCodeOrThrow(String groupId, String codeId) {
        getGroupOrThrow(groupId);
        CommonCode commonCode = commonCodeMapper.selectCommonCodeById(groupId, codeId);
        if (commonCode == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Common code not found");
        }
        return commonCode;
    }

    private User requireAdminUser() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user;
    }
}
