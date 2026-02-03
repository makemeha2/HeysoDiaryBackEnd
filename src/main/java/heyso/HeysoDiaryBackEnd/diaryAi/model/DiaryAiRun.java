package heyso.HeysoDiaryBackEnd.diaryAi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiRunStatus;
import heyso.HeysoDiaryBackEnd.diaryAi.model.enums.DiaryAiTriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAiRun {

    private Long runId;
    private Long diaryId;
    private Long userId;

    private DiaryAiTriggerType triggerType;
    private DiaryAiRunStatus status;

    private String model;
    private Double temperature;
    private Double topP;
    private Integer maxOutputTokens;

    private String requestId;

    private String promptSystem;
    private String promptUser;

    private String promptHash;
    private LocalDateTime diaryUpdatedAtSnapshot;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private BigDecimal costUsd;

    private String errorCode;
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
