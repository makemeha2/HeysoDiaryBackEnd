package heyso.HeysoDiaryBackEnd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiaryCreateResponse {

    private Long diaryId;

    public static DiaryCreateResponse of(Long diaryId) {
        return new DiaryCreateResponse(diaryId);
    }
}
