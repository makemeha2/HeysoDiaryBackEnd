package heyso.HeysoDiaryBackEnd.user.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {
    private Long userId; // tb_user.user_id
    private String email;
    private String nickname;
    private String role; // ADMIN / MEMBER
    private String status; // ACTIVE / INACTIVE / BLOCKED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
