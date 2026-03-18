package heyso.HeysoDiaryBackEnd.auth.model;

import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.auth.service.ReauthPurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailOtp {
    private Long otpId;
    private Long userId;
    private ReauthPurpose purpose;
    private String email;
    private String otpHash;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime consumedAt;
    private String sendStatus;
    private Integer failCount;
    private Integer resendCount;
    private LocalDateTime lastSentAt;
    private String requestIp;
    private String requestUa;
    private String verifyIp;
    private String verifyUa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
