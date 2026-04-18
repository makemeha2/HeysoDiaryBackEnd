package heyso.HeysoDiaryBackEnd.userMng.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserDetailResponse {
    private Long userId;
    private Long userAuthId;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String authProvider;
    private String loginId;
    private String providerUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
