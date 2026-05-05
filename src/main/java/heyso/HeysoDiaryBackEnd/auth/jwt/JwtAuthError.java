package heyso.HeysoDiaryBackEnd.auth.jwt;

public enum JwtAuthError {
    EXPIRED("expired"),
    INVALID("invalid"),
    REVOKED("revoked"),
    INACTIVE("inactive");

    public static final String REQUEST_ATTRIBUTE = "heyso.jwt.auth_error";
    public static final String RESPONSE_HEADER = "X-Auth-Error";

    private final String headerValue;

    JwtAuthError(String headerValue) {
        this.headerValue = headerValue;
    }

    public String headerValue() {
        return headerValue;
    }
}
