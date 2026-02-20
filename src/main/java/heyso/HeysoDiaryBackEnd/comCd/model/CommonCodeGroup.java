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
public class CommonCodeGroup {
    private String groupId;
    private String groupName;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private Long createdId;
    private LocalDateTime updatedAt;
    private Long updatedId;
}
