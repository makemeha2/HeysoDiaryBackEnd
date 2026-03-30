package heyso.HeysoDiaryBackEnd.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    @NotBlank
    @Size(max = 100)
    private String loginId;

    @NotBlank
    @Size(max = 200)
    private String password;
}
