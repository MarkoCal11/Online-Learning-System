package hr.javafx.onlinelearningsystem.exception;

public class UserDataFormatException extends RuntimeException {
    public UserDataFormatException() {
    }
    public UserDataFormatException(String message) {
        super(message);
    }
    public UserDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserDataFormatException(Throwable cause) {
        super(cause);
    }
    public UserDataFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
