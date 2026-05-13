package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAnalysisCandidate {
    private Long diaryId;
    private Long userId;
    private String title;
    private String contentMd;
    private LocalDate diaryDate;
    private String moodId;
    private LocalDateTime diaryUpdatedAt;
    private String contentHash;
    private LocalDateTime contentUpdatedAt;
}
