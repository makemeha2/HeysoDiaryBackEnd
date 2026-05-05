package heyso.HeysoDiaryBackEnd.auth.jwt;

public class JwtAuthException extends RuntimeException {
    private final JwtAuthError error;

    public JwtAuthException(JwtAuthError error, String message) {
        super(message);
        this.error = error;
    }

    public JwtAuthError getError() {
        return error;
    }
}
