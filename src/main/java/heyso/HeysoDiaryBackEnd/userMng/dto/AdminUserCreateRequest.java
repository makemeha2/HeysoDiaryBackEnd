package heyso.HeysoDiaryBackEnd.userMng.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserCreateRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 50)
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,30}$", message = "loginId is invalid")
    private String loginId;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    @Pattern(regexp = "ADMIN|MEMBER", message = "role is invalid")
    private String role;
}
