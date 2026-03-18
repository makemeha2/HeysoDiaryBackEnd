package heyso.HeysoDiaryBackEnd.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReauthStatusResponse {
    private final String purpose;
    private final boolean verified;
    private final LocalDateTime verifiedUntil;
}
