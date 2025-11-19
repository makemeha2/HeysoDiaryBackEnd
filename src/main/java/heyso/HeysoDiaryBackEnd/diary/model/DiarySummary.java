package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DiarySummary {

    private Long diaryId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String contentMd;
    private LocalDate diaryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
