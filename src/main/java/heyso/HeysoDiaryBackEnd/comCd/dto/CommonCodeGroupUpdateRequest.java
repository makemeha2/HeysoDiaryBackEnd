package heyso.HeysoDiaryBackEnd.comCd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonCodeGroupUpdateRequest {
    @NotBlank
    @Size(max = 300)
    private String groupName;

    @NotNull
    private Boolean isActive;
}
