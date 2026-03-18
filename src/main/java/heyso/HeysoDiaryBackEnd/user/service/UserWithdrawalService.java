package heyso.HeysoDiaryBackEnd.user.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserMapper userMapper;

    @Transactional
    public void withdraw(Long userId, String reasonCode, String reasonText) {
        String anonymizedEmail = buildAnonymizedEmail(userId);
        int updatedRows = userMapper.withdrawUser(userId, anonymizedEmail, reasonCode, reasonText);
        if (updatedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already withdrawn or unavailable");
        }

        userMapper.deleteUserAuthByUserId(userId);
    }

    private String buildAnonymizedEmail(Long userId) {
        return "withdrawn+" + userId + "." + Instant.now().toEpochMilli() + "@heysodiary.local";
    }
}
