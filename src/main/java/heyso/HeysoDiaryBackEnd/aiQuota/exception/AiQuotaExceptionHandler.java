package heyso.HeysoDiaryBackEnd.aiQuota.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiQuotaExceptionHandler {

    @ExceptionHandler(AiQuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleAiQuotaExceeded(AiQuotaExceededException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("remainingCount", 0);
        body.put("dailyLimit", exception.getDailyLimit());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}
