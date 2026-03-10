package heyso.HeysoDiaryBackEnd.auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReauthVerifyResponse {
    private final String purpose;
    private final Instant verifiedUntil;
}
