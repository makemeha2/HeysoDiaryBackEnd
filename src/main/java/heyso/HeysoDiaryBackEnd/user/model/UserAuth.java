package heyso.HeysoDiaryBackEnd.user.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserAuth {
    private Long userAuthId; // tb_user_auth.user_auth_id
    private Long userId; // tb_user.user_id

    private String authProvider; // LOCAL / GOOGLE / NAVER
    private String providerUserId;

    private String loginId; // LOCAL 전용
    private String passwordHash; // LOCAL 전용

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
