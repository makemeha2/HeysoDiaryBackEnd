package heyso.HeysoDiaryBackEnd.diaryAi.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAiCommentCreateRequest {

    private String model;

    @Positive
    private Integer recentLimit;

    @Positive
    private Integer tagLimit;

    @Positive
    private Integer similarLimit;

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private BigDecimal temperature;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal topP;

    @Positive
    private Integer maxOutputTokens;
}

