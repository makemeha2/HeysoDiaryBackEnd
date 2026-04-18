package heyso.HeysoDiaryBackEnd.userMng.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusUpdateRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED", message = "status is invalid")
    private String status;
}
