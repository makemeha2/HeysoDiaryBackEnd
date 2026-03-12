package heyso.HeysoDiaryBackEnd.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailReauthVerifyRequest {
    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "OTP must be a 4-digit number")
    private String otp;
}
