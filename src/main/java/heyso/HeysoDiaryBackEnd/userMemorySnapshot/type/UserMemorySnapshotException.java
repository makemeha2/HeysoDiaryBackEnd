package heyso.HeysoDiaryBackEnd.userMemorySnapshot.type;

public class UserMemorySnapshotException extends RuntimeException {
    public UserMemorySnapshotException(String message) {
        super(message);
    }

    public UserMemorySnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}
