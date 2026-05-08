package heyso.HeysoDiaryBackEnd.diaryAiPolish.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiaryAiPolishResponse {

    private Long polishLogId;
    private String originalContent;
    private String polishedContent;
    private Integer remainingCount;
    private Integer dailyLimit;
    private boolean applied;
    private String status;

    public static DiaryAiPolishResponse of(Long polishLogId,
            String originalContent,
            String polishedContent,
            Integer remainingCount,
            Integer dailyLimit,
            boolean applied,
            String status) {
        return new DiaryAiPolishResponse(polishLogId, originalContent, polishedContent, remainingCount, dailyLimit,
                applied, status);
    }
}
