package dev.codebymelendez.notifications.api.exception;

import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {
    
    private final UUID notificationId;
    
    public NotificationNotFoundException(UUID notificationId) {
        super(
            String.format("Notificación no encontrada: %s", notificationId),
            "NOTIFICATION_NOT_FOUND"
        );
        this.notificationId = notificationId;
    }
    
    public NotificationNotFoundException(String message) {
        super(message, "NOTIFICATION_NOT_FOUND");
        this.notificationId = null;
    }
    
    public UUID getNotificationId() {
        return notificationId;
    }
}
