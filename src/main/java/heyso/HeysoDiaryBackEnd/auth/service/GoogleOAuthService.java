package heyso.HeysoDiaryBackEnd.auth.service;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.google.client-id}")
    private String googleClientId;

    public AuthResponse loginOrRegister(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);

        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String name = (String) payload.get("name");

        if (!emailVerified) {
            throw new IllegalArgumentException("Google email is not verified");
        }

        // 1) tb_user_auth에서 먼저 조회
        UserAuth userAuth = userMapper
                .selectUserAuthByProviderAndProviderUserId("GOOGLE", googleSub);

        User user;
        if (userAuth == null) {
            // 신규 회원가입
            user = registerNewGoogleUser(googleSub, email, name);
        } else {
            // 기존 유저
            user = userMapper.selectUserById(userAuth.getUserId());
            userMapper.updateUserAuthLastLoginAt(userAuth.getUserAuthId());
        }

        String token = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole());
    }

    private User registerNewGoogleUser(String googleSub, String email, String name) {
        // tb_user insert
        User user = new User();
        user.setEmail(email);
        user.setNickname(name != null ? name : email);
        user.setRole("MEMBER");
        user.setStatus("ACTIVE");
        userMapper.insertUser(user); // userId 생성됨

        // tb_user_auth insert
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(user.getUserId());
        userAuth.setAuthProvider("GOOGLE");
        userAuth.setProviderUserId(googleSub);
        userAuth.setLoginId(null);
        userAuth.setPasswordHash(null);
        userMapper.insertUserAuth(userAuth);

        return user;
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }
}
