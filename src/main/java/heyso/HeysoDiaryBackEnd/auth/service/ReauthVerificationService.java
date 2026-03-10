package heyso.HeysoDiaryBackEnd.auth.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReauthVerificationService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final ReauthStateStore reauthStateStore;

    public Instant markVerified(Long userId, String purpose) {
        return reauthStateStore.saveVerified(userId, purpose, DEFAULT_TTL);
    }

    public boolean isVerified(Long userId, String purpose) {
        return reauthStateStore.isVerified(userId, purpose);
    }

    public boolean consume(Long userId, String purpose) {
        return reauthStateStore.consume(userId, purpose);
    }
}
