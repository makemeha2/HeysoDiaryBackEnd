package heyso.HeysoDiaryBackEnd.diaryAi.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiaryAiCommentCreateResponse {

    private Long runId;
    private Long aiCommentId;
    private String contentMd;
    private LocalDateTime createdAt;
    private Integer remainingCount;
    private Integer dailyLimit;

    public static DiaryAiCommentCreateResponse of(
        Long runId,
        Long aiCommentId,
        String contentMd,
        LocalDateTime createdAt,
        Integer remainingCount,
        Integer dailyLimit
    ) {
        return new DiaryAiCommentCreateResponse(runId, aiCommentId, contentMd, createdAt, remainingCount, dailyLimit);
    }
}

