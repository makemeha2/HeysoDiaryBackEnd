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

    public static DiaryAiCommentCreateResponse of(
        Long runId,
        Long aiCommentId,
        String contentMd,
        LocalDateTime createdAt
    ) {
        return new DiaryAiCommentCreateResponse(runId, aiCommentId, contentMd, createdAt);
    }
}

