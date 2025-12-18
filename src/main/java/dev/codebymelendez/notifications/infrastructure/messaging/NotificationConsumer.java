package dev.codebymelendez.notifications.infrastructure.messaging;

import dev.codebymelendez.notifications.api.exception.NotificationNotFoundException;
import dev.codebymelendez.notifications.api.exception.UnsupportedChannelException;
import dev.codebymelendez.notifications.application.event.NotificationEvent;
import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.DeliveryResult;
import dev.codebymelendez.notifications.domain.model.Notification;
import dev.codebymelendez.notifications.domain.model.NotificationStatus;
import dev.codebymelendez.notifications.domain.repository.NotificationRepository;
import dev.codebymelendez.notifications.infrastructure.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Component
public class NotificationConsumer {
    
    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> senders;
    
    @Value("${app.retry.max-attempts:3}")
    private int maxAttempts;
    
    public NotificationConsumer(
            NotificationRepository notificationRepository,
            List<NotificationSender> senders) {
        this.notificationRepository = notificationRepository;
        this.senders = senders;

        log.info("NotificationConsumer inicializado con {} senders: {}",
                senders.size(),
                senders.stream()
                        .map(s -> s.getChannel().name())
                        .toList());
    }
    
    @RabbitListener(queues = "${app.rabbitmq.queue.notifications}")
    @Transactional
    public void processNotification(NotificationEvent event) {
        log.info("Procesando notificación: id={}, attempt={}, correlationId={}", 
                event.getNotificationId(), 
                event.getAttemptNumber(),
                event.getCorrelationId());
        
        try {
            Notification notification = loadNotification(event);

            if (notification.getStatus().isTerminal()) {
                log.warn("Notificación ya en estado terminal, ignorando: id={}, status={}", 
                        notification.getId(), notification.getStatus());
                return;
            }

            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);
            
            NotificationSender sender = findSender(notification.getChannel());
            
            DeliveryResult result = sender.send(notification);
            
            handleResult(notification, result, event);
            
        } catch (NotificationNotFoundException e) {
            log.error("Notificación no encontrada, descartando mensaje: id={}",
                    event.getNotificationId());
        } catch (UnsupportedChannelException e) {
            log.error("Canal no soportado, descartando mensaje: id={}, channel={}",
                    event.getNotificationId(), e.getChannel());
            markAsFailed(event.getNotificationId(), e.getMessage());
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.dlq}")
    @Transactional
    public void handleDeadLetter(NotificationEvent event) {
        log.error("Notificación en DLQ después de {} intentos: id={}, correlationId={}", 
                event.getAttemptNumber(),
                event.getNotificationId(),
                event.getCorrelationId());

        markAsFailed(event.getNotificationId(), 
                "Máximo de reintentos excedido después de " + event.getAttemptNumber() + " intentos");
    }

    private Notification loadNotification(NotificationEvent event) {
        return notificationRepository.findById(event.getNotificationId())
                .orElseThrow(() -> new NotificationNotFoundException(event.getNotificationId()));
    }
    
    private NotificationSender findSender(Channel channel) {
        return senders.stream()
                .filter(sender -> sender.supports(channel))
                .filter(NotificationSender::isEnabled)
                .findFirst()
                .orElseThrow(() -> new UnsupportedChannelException(channel));
    }
    
    private void handleResult(Notification notification, DeliveryResult result, NotificationEvent event) {
        notification.recordAttempt(result.success(), result.errorMessage());
        
        if (result.success()) {
            notification.markAsDelivered();
            notificationRepository.save(notification);
            
            log.info("Notificación entregada exitosamente: id={}, channel={}, attempts={}", 
                    notification.getId(), 
                    notification.getChannel(),
                    notification.getAttemptCount());
        } else {
            if (notification.canRetry(maxAttempts)) {
                notification.setStatus(NotificationStatus.QUEUED);
                notificationRepository.save(notification);
                
                log.warn("Intento fallido, reintentando: id={}, attempt={}/{}, error={}", 
                        notification.getId(),
                        notification.getAttemptCount(),
                        maxAttempts,
                        result.errorMessage());
                throw new RuntimeException("Reintento requerido: " + result.errorMessage());
                
            } else {
                notification.markAsFailed(result.errorMessage());
                notificationRepository.save(notification);
                log.error("Notificación fallida después de {} intentos: id={}, error={}", 
                        notification.getAttemptCount(),
                        notification.getId(),
                        result.errorMessage());
            }
        }
    }

    private void markAsFailed(java.util.UUID notificationId, String reason) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.getStatus().isTerminal()) {
                notification.markAsFailed(reason);
                notificationRepository.save(notification);
            }
        });
    }
}
