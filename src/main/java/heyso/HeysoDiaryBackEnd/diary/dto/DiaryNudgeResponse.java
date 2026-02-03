package heyso.HeysoDiaryBackEnd.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryNudgeResponse {
    private String messageKey;
    private String messageText;
}
