package heyso.HeysoDiaryBackEnd.userMng.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String nickname;

    @NotBlank
    @Pattern(regexp = "ADMIN|MEMBER", message = "role is invalid")
    private String role;
}
