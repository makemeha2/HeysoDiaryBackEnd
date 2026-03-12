package heyso.HeysoDiaryBackEnd.auth.service.email;

public interface EmailSender {
    void sendEmail(String toEmail, String subject, String body);

    default void sendAccountDeleteOtp(String toEmail, String otpCode) {
        String subject = "[Heyso Diary] Account Deletion OTP";
        String body = "Your OTP code is: " + otpCode + "\nThis code expires in 10 minutes.";
        sendEmail(toEmail, subject, body);
    }
}
