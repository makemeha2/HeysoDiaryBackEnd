package heyso.HeysoDiaryBackEnd.auth.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenDenylistEntry {
    private String jti;
    private Long userId;
    private LocalDateTime expiresAt;
    private String revokedReason;
    private LocalDateTime createdAt;
}
