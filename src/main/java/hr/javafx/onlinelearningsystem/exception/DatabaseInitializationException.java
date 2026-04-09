package hr.javafx.onlinelearningsystem.exception;

public class DatabaseInitializationException extends RuntimeException {
    public DatabaseInitializationException() {
    }
    public DatabaseInitializationException(String message) {
        super(message);
    }
    public DatabaseInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseInitializationException(Throwable cause) {
        super(cause);
    }
    public DatabaseInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
