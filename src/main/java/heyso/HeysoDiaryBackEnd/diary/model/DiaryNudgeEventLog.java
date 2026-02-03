package heyso.HeysoDiaryBackEnd.diary.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class DiaryNudgeEventLog {

    private Long id;
    private Long userId;
    private LocalDate localDate;
    private String eventType;
    private String messageKey;
    private String messageText;
    private LocalDateTime clientTime;
    private LocalDateTime createdAt;
    private String metadataJson;
}
