package heyso.HeysoDiaryBackEnd.diaryAi.model;

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
public class DiaryAiComment {

    private Long aiCommentId;

    private Long diaryId;
    private Long userId;
    private Long runId;

    private String contentMd;
    private Boolean isPinned;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

