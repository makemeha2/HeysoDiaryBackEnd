package heyso.HeysoDiaryBackEnd.diaryAnalysis.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryAnalysis {
    private Long analysisId;
    private Long diaryId;
    private Long userId;
    private Integer analysisVersion;
    private String status;
    private Boolean active;
    private String contentHash;
    private LocalDateTime diaryUpdatedAtSnapshot;
    private Long bindingId;
    private Long systemTemplateId;
    private Long userTemplateId;
    private Long runtimeProfileId;
    private String rawResponseJson;
    private String summaryText;
    private String errorCode;
    private String errorMessage;
}
