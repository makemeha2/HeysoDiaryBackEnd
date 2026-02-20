package heyso.HeysoDiaryBackEnd.comCd.dto;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonCodeResponse {
    private String groupId;
    private String codeId;
    private String codeName;
    private Boolean isActive;
    private String extraInfo1;
    private String extraInfo2;
    private Integer sortSeq;
    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;

    public static CommonCodeResponse from(CommonCode commonCode) {
        return CommonCodeResponse.builder()
                .groupId(commonCode.getGroupId())
                .codeId(commonCode.getCodeId())
                .codeName(commonCode.getCodeName())
                .isActive(commonCode.getIsActive())
                .extraInfo1(commonCode.getExtraInfo1())
                .extraInfo2(commonCode.getExtraInfo2())
                .sortSeq(commonCode.getSortSeq())
                .createdAt(commonCode.getCreatedAt())
                .createdId(commonCode.getCreatedId())
                .updatedAt(commonCode.getUpdatedAt())
                .updatedId(commonCode.getUpdatedId())
                .build();
    }
}
