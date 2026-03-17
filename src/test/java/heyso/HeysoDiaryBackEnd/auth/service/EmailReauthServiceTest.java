package heyso.HeysoDiaryBackEnd.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.auth.dto.ReauthStatusResponse;
import heyso.HeysoDiaryBackEnd.auth.mapper.EmailReauthMapper;
import heyso.HeysoDiaryBackEnd.auth.model.ReauthGrant;
import heyso.HeysoDiaryBackEnd.mail.sender.EmailSender;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
class EmailReauthServiceTest {

    @Mock
    private EmailReauthMapper emailReauthMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private EmailReauthService emailReauthService;

    @Test
    void getReauthStatus_returnsFalse_whenGrantNotExists() {
        // given
        when(emailReauthMapper.selectActiveGrant(1L, ReauthPurpose.ACCOUNT_DELETE)).thenReturn(null);

        // when
        ReauthStatusResponse response = emailReauthService.getReauthStatus(1L, ReauthPurpose.ACCOUNT_DELETE);

        // then
        assertThat(response.isVerified()).isFalse();
        assertThat(response.getPurpose()).isEqualTo(ReauthPurpose.ACCOUNT_DELETE.name());
        assertThat(response.getVerifiedUntil()).isNull();
    }

    @Test
    void getReauthStatus_returnsTrue_whenGrantExists() {
        // given
        ReauthGrant grant = new ReauthGrant();
        grant.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        when(emailReauthMapper.selectActiveGrant(1L, ReauthPurpose.ACCOUNT_DELETE)).thenReturn(grant);

        // when
        ReauthStatusResponse response = emailReauthService.getReauthStatus(1L, ReauthPurpose.ACCOUNT_DELETE);

        // then
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getVerifiedUntil()).isNotNull();
    }
}
