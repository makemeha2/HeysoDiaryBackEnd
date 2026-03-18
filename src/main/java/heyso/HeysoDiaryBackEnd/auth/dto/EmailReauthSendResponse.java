package heyso.HeysoDiaryBackEnd.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailReauthSendResponse {
    private final String purpose;
    private final LocalDateTime otpExpiresAt;
    private final String maskedEmail;
}
