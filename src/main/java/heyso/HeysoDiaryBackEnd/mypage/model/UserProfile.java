package heyso.HeysoDiaryBackEnd.mypage.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long userId;
    private String nickname;
    private String mbti;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
