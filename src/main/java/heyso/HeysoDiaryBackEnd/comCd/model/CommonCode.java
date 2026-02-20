package heyso.HeysoDiaryBackEnd.comCd.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonCode {
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
}
