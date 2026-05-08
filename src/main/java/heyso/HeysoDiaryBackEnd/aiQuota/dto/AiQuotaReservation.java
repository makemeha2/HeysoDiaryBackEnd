package heyso.HeysoDiaryBackEnd.aiQuota.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiQuotaReservation {

    private AiQuotaStatusResponse statusResponse;
    private Long usageLogId;
}
