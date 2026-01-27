package heyso.HeysoDiaryBackEnd.diaryAi.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiaryAiCommentListItemResponse {

    private Long aiCommentId;
    private Long diaryId;
    private Long runId;
    private String contentMd;
    private Boolean isPinned;
    private LocalDateTime createdAt;

    public static DiaryAiCommentListItemResponse of(
        Long aiCommentId,
        Long diaryId,
        Long runId,
        String contentMd,
        Boolean isPinned,
        LocalDateTime createdAt
    ) {
        return new DiaryAiCommentListItemResponse(aiCommentId, diaryId, runId, contentMd, isPinned, createdAt);
    }
}

