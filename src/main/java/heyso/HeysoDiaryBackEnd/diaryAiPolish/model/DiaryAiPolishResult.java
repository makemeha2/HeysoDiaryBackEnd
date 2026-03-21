package heyso.HeysoDiaryBackEnd.diaryAiPolish.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiPolishResult {

    private Long id;
    private Long polishLogId;
    private Long userId;
    private Long diaryId;
    private String originalContent;
    private String polishedContent;
    private String appliedYn;
    private String savedYn;
    private LocalDateTime createdAt;
    private LocalDateTime appliedAt;
    private LocalDateTime savedAt;
}
