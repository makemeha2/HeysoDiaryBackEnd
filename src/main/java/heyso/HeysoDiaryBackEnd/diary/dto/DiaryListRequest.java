package heyso.HeysoDiaryBackEnd.diary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryListRequest {

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 20;

    @Positive
    private Long userId;

    public int getOffset() {
        return (Math.max(page, 1) - 1) * Math.max(size, 1);
    }
}
