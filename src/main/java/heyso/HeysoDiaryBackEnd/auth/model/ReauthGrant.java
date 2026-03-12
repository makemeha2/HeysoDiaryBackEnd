package heyso.HeysoDiaryBackEnd.auth.model;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.auth.service.ReauthGrantType;
import heyso.HeysoDiaryBackEnd.auth.service.ReauthPurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReauthGrant {
    private Long grantId;
    private Long userId;
    private ReauthPurpose purpose;
    private ReauthGrantType grantedByType;
    private Long sourceOtpId;
    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createdAt;
}
