package heyso.HeysoDiaryBackEnd.auth.service;

import java.time.Duration;
import java.time.Instant;

public interface ReauthStateStore {

    Instant saveVerified(Long userId, String purpose, Duration ttl);

    boolean isVerified(Long userId, String purpose);

    boolean consume(Long userId, String purpose);
}
