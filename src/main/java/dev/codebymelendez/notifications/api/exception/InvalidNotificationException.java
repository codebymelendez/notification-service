package dev.codebymelendez.notifications.api.exception;


public class InvalidNotificationException extends NotificationException {
    
    private final String field;
    
    public InvalidNotificationException(String message) {
        super(message, "INVALID_NOTIFICATION");
        this.field = null;
    }
    
    public InvalidNotificationException(String field, String message) {
        super(message, "INVALID_NOTIFICATION");
        this.field = field;
    }
    
    /**
     * Campo que tiene el valor inválido (si aplica).
     */
    public String getField() {
        return field;
    }
}
