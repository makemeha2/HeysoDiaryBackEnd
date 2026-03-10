package heyso.HeysoDiaryBackEnd.auth.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.dto.ReauthVerifyResponse;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthReauthService {

    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final GoogleOAuthService googleOAuthService;
    private final UserMapper userMapper;
    private final ReauthVerificationService reauthVerificationService;

    public ReauthVerifyResponse verifyGoogleForWithdraw(Long userId, String idToken) {
        String tokenSub = googleOAuthService.extractGoogleSubject(idToken);
        UserAuth userAuth = userMapper.selectUserAuthByUserIdAndProvider(userId, GOOGLE_PROVIDER);

        if (userAuth == null || userAuth.getProviderUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Google auth provider is not linked");
        }

        if (!tokenSub.equals(userAuth.getProviderUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Google account does not match current user");
        }

        Instant verifiedUntil = reauthVerificationService.markVerified(userId, ReauthPurpose.WITHDRAW);
        return new ReauthVerifyResponse(ReauthPurpose.WITHDRAW, verifiedUntil);
    }
}
