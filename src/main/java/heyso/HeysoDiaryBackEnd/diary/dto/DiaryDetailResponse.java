package heyso.HeysoDiaryBackEnd.diary.dto;

import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryDetailResponse {

    private Long diaryId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String contentMd;
    private LocalDate diaryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;

    public static DiaryDetailResponse from(DiarySummary diarySummary, List<String> tags) {
        DiaryDetailResponse response = new DiaryDetailResponse();
        response.setDiaryId(diarySummary.getDiaryId());
        response.setAuthorId(diarySummary.getAuthorId());
        response.setAuthorNickname(diarySummary.getAuthorNickname());
        response.setTitle(diarySummary.getTitle());
        response.setContentMd(diarySummary.getContentMd());
        response.setDiaryDate(diarySummary.getDiaryDate());
        response.setCreatedAt(diarySummary.getCreatedAt());
        response.setUpdatedAt(diarySummary.getUpdatedAt());
        response.setTags(tags);
        return response;
    }
}
