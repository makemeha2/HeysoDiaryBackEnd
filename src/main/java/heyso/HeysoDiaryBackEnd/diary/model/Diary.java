package heyso.HeysoDiaryBackEnd.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Diary {

    private Long diaryId;
    private Long userId;
    private String title;
    private String contentMd;
    private LocalDate diaryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
