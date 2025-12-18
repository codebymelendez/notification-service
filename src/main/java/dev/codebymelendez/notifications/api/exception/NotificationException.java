package dev.codebymelendez.notifications.api.exception;


public class NotificationException extends RuntimeException {
    
    private final String errorCode;
    
    public NotificationException(String message) {
        super(message);
        this.errorCode = "NOTIFICATION_ERROR";
    }
    
    public NotificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "NOTIFICATION_ERROR";
    }
    
    public NotificationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
