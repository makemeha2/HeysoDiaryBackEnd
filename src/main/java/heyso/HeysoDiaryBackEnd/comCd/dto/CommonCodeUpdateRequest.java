package heyso.HeysoDiaryBackEnd.comCd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonCodeUpdateRequest {
    @NotBlank
    @Size(max = 300)
    private String codeName;

    @NotNull
    private Boolean isActive;

    @Size(max = 255)
    private String extraInfo1;

    @Size(max = 255)
    private String extraInfo2;

    @NotNull
    private Integer sortSeq;
}
