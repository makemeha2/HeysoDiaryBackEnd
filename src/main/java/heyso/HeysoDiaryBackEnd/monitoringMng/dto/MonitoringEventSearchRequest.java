package heyso.HeysoDiaryBackEnd.monitoringMng.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringEventSearchRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Pattern(regexp = "Y|N", message = "resolvedYn must be Y or N")
    private String resolvedYn;

    @Pattern(regexp = "ERROR|WARN|INFO|SECURITY|BUSINESS", message = "eventType is invalid")
    private String eventType;

    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "severity is invalid")
    private String severity;

    private String keyword;

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 20;

    @Schema(hidden = true)
    public int getOffset() {
        return (Math.max(page, 1) - 1) * Math.max(size, 1);
    }

    @AssertTrue(message = "endDate must be on or after startDate")
    @Schema(hidden = true)
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
