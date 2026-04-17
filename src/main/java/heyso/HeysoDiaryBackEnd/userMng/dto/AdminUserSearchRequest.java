package heyso.HeysoDiaryBackEnd.userMng.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSearchRequest {

    private String keyword;

    @Pattern(regexp = "ADMIN|MEMBER", message = "role is invalid")
    private String role;

    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED|WITHDRAWN", message = "status is invalid")
    private String status;

    @Pattern(regexp = "LOCAL|GOOGLE|NAVER", message = "authProvider is invalid")
    private String authProvider;

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 20;

    @Schema(hidden = true)
    public int getOffset() {
        return (Math.max(page, 1) - 1) * Math.max(size, 1);
    }
}
