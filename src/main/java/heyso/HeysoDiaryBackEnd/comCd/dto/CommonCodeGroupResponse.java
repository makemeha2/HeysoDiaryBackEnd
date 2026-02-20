package heyso.HeysoDiaryBackEnd.comCd.dto;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.comCd.model.CommonCodeGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonCodeGroupResponse {
    private String groupId;
    private String groupName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;

    public static CommonCodeGroupResponse from(CommonCodeGroup group) {
        return CommonCodeGroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .isActive(group.getIsActive())
                .createdAt(group.getCreatedAt())
                .createdId(group.getCreatedId())
                .updatedAt(group.getUpdatedAt())
                .updatedId(group.getUpdatedId())
                .build();
    }
}
