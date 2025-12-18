package dev.codebymelendez.notifications.infrastructure.sender;

import dev.codebymelendez.notifications.domain.model.DeliveryResult;
import dev.codebymelendez.notifications.domain.model.Notification;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class AbstractNotificationSender implements NotificationSender {
    
    @Override
    public final DeliveryResult send(Notification notification) {
        String channelName = getChannel().getDisplayName();
        
        log.info("[{}] Iniciando envío: notificationId={}, recipient={}", 
                channelName, notification.getId(), notification.getRecipient());
        
        try {
            validate(notification);

            DeliveryResult result = doSend(notification);

            if (result.success()) {
                log.info("[{}] Envío exitoso: notificationId={}", 
                        channelName, notification.getId());
            } else {
                log.warn("[{}] Envío fallido: notificationId={}, error={}", 
                        channelName, notification.getId(), result.errorMessage());
            }
            
            return result;
            
        } catch (IllegalArgumentException e) {
            log.warn("[{}] Validación fallida: notificationId={}, error={}", 
                    channelName, notification.getId(), e.getMessage());
            return DeliveryResult.failure(e.getMessage(), "VALIDATION_ERROR");
            
        } catch (Exception e) {
            log.error("[{}] Error inesperado: notificationId={}, error={}", 
                    channelName, notification.getId(), e.getMessage(), e);
            return DeliveryResult.fromException(e);
        }
    }
    
    protected void validate(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("La notificación no puede ser null");
        }
        
        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            throw new IllegalArgumentException("El destinatario es obligatorio");
        }
        
        if (notification.getContent() == null || notification.getContent().isBlank()) {
            throw new IllegalArgumentException("El contenido es obligatorio");
        }
    }
    
    protected abstract DeliveryResult doSend(Notification notification);
}
