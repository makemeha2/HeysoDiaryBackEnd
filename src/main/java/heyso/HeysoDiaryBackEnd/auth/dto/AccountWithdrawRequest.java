package heyso.HeysoDiaryBackEnd.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountWithdrawRequest {
    @Size(max = 50)
    private String reasonCode;

    @Size(max = 500)
    private String reasonText;
}
