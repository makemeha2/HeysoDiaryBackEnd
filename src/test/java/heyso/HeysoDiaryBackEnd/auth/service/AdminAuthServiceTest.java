package heyso.HeysoDiaryBackEnd.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.dto.AuthResponse;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        UserAuth userAuth = new UserAuth();
        userAuth.setUserAuthId(1L);
        userAuth.setUserId(1L);
        userAuth.setPasswordHash("$2a$10$HNOpwqwgkJhL8iRanNxBcerU376kgMg3qfzzTpFdI2niS.wOAUZua");

        User user = new User();
        user.setUserId(1L);
        user.setEmail("admin@example.com");
        user.setNickname("관리자");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");

        when(userMapper.selectUserAuthByLoginIdAndProvider("admin", "LOCAL")).thenReturn(userAuth);
        when(userMapper.selectUserById(1L)).thenReturn(user);
        when(jwtTokenProvider.generateToken(1L, "admin@example.com", "ADMIN", "admin")).thenReturn("token");

        AuthResponse response = adminAuthService.login("admin", "1234");

        assertThat(response.getAccessToken()).isEqualTo("token");
        verify(userMapper).updateUserAuthLastLoginAt(1L);
    }

    @Test
    void login_throwsUnauthorized_whenStoredHashIsInvalidFormat() {
        UserAuth userAuth = new UserAuth();
        userAuth.setPasswordHash("$2a$10$LOCAL_ADMIN_HASH_EXAMPLE");

        when(userMapper.selectUserAuthByLoginIdAndProvider("admin", "LOCAL")).thenReturn(userAuth);

        assertThatThrownBy(() -> adminAuthService.login("admin", "1234"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode().value()).isEqualTo(401);
                    assertThat(responseStatusException.getReason()).isEqualTo("Invalid admin credentials");
                });
    }
}
