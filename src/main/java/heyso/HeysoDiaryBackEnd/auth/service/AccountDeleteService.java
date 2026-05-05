package heyso.HeysoDiaryBackEnd.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.user.service.UserWithdrawalService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountDeleteService {

    private final EmailReauthService emailReauthService;
    private final UserWithdrawalService userWithdrawalService;
    private final AuthTokenService authTokenService;

    @Transactional
    public void deleteAccount(Long userId, String reasonCode, String reasonText) {
        emailReauthService.consumeActiveGrant(userId, ReauthPurpose.ACCOUNT_DELETE);
        authTokenService.revokeAllUserTokens(userId);
        userWithdrawalService.withdraw(userId, reasonCode, reasonText);
    }
}
