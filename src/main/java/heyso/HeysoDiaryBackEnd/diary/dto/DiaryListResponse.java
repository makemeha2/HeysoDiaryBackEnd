package heyso.HeysoDiaryBackEnd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DiaryListResponse {

    private List<DiarySummaryResponse> diaries;

    public static DiaryListResponse of(List<DiarySummaryResponse> diaries) {
        return new DiaryListResponse(diaries);
    }
}
