package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDiaryContentResponse {
    private Long diaryId;
    private Long userId;
    private String authorNickname;
    private String title;
    private String contentMd;
    private LocalDate diaryDate;
    private String moodId;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
