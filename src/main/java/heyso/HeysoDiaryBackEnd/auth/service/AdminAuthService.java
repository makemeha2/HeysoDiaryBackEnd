package heyso.HeysoDiaryBackEnd.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private static final String ADMIN_SCOPE = "admin";
    private static final String LOCAL_PROVIDER = "LOCAL";

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse login(String loginId, String password) {
        UserAuth userAuth = userMapper.selectUserAuthByLoginIdAndProvider(loginId, LOCAL_PROVIDER);
        if (userAuth == null || userAuth.getPasswordHash() == null
                || !passwordEncoder.matches(password, userAuth.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        User user = userMapper.selectUserById(userAuth.getUserId());
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inactive admin account");
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }

        userMapper.updateUserAuthLastLoginAt(userAuth.getUserAuthId());
        String token = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                ADMIN_SCOPE);

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                null);
    }
}
