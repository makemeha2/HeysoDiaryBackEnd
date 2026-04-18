package heyso.HeysoDiaryBackEnd.userMng.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserPasswordResetRequest {

    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
