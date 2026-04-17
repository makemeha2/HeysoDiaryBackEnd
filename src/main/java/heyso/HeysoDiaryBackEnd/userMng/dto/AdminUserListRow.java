package heyso.HeysoDiaryBackEnd.userMng.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserListRow {
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String authProvider;
    private String loginId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
