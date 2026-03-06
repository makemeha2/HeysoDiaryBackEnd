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
public class UserAIFeedbackSetting {
    private Long userId;
    private String speechToneCd;
    private String feedbackStyleCd;
    private String intensityCd;
    private String questionCd;
    private String lengthCd;
    private String langModeCd;
    private String fixedLang;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
