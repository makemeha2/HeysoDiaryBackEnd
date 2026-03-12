package heyso.HeysoDiaryBackEnd.auth.service.email;

public interface EmailSender {
    void sendAccountDeleteOtp(String toEmail, String otpCode);
}
