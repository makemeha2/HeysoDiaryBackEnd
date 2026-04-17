package heyso.HeysoDiaryBackEnd.userMng.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

@Getter
public class AdminUserConflictException extends ResponseStatusException {

    private final String errorCode;

    public AdminUserConflictException(String errorCode, String reason) {
        super(HttpStatus.CONFLICT, reason);
        this.errorCode = errorCode;
    }
}
