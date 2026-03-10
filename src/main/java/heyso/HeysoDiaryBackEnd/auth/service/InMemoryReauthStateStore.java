package heyso.HeysoDiaryBackEnd.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryReauthStateStore implements ReauthStateStore {

    private final Map<ReauthKey, Instant> expiresAtMap = new ConcurrentHashMap<>();

    @Override
    public Instant saveVerified(Long userId, String purpose, Duration ttl) {
        Instant expiresAt = Instant.now().plus(ttl);
        expiresAtMap.put(new ReauthKey(userId, purpose), expiresAt);
        return expiresAt;
    }

    @Override
    public boolean isVerified(Long userId, String purpose) {
        return isActive(new ReauthKey(userId, purpose));
    }

    @Override
    public boolean consume(Long userId, String purpose) {
        ReauthKey key = new ReauthKey(userId, purpose);
        if (!isActive(key)) {
            return false;
        }
        expiresAtMap.remove(key);
        return true;
    }

    private boolean isActive(ReauthKey key) {
        Instant expiresAt = expiresAtMap.get(key);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isAfter(Instant.now())) {
            return true;
        }
        expiresAtMap.remove(key, expiresAt);
        return false;
    }

    private record ReauthKey(Long userId, String purpose) {
    }
}
