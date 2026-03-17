package heyso.HeysoDiaryBackEnd.mail.template;

public record EmailTemplate(
        String subject,
        String htmlBody,
        String textBody) {
}
