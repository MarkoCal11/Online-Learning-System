package hr.javafx.onlinelearningsystem.exception;

public class InvalidLoginCredentialsException extends RuntimeException {
    public InvalidLoginCredentialsException() {}
    public InvalidLoginCredentialsException(String message) {
        super(message);
    }
    public InvalidLoginCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidLoginCredentialsException(Throwable cause) {
        super(cause);
    }
    public InvalidLoginCredentialsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
