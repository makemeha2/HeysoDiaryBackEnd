package heyso.HeysoDiaryBackEnd.mail.sender;

public interface EmailSender {
    void sendEmail(String toEmail, String subject, String htmlBody, String textBody);
}
