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

    @Transactional
    public void deleteAccount(Long userId, String reasonCode, String reasonText) {
        emailReauthService.consumeActiveGrant(userId, ReauthPurpose.ACCOUNT_DELETE);
        userWithdrawalService.withdraw(userId, reasonCode, reasonText);
    }
}
