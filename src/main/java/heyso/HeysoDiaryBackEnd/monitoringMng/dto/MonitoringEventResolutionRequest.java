package heyso.HeysoDiaryBackEnd.monitoringMng.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringEventResolutionRequest {

    @NotEmpty
    private List<@NotNull Long> eventIds;

    @NotNull
    @Pattern(regexp = "Y|N", message = "resolvedYn must be Y or N")
    private String resolvedYn;
}
