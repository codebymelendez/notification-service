package dev.codebymelendez.notifications.application;

import dev.codebymelendez.notifications.api.dto.NotificationRequest;
import dev.codebymelendez.notifications.application.event.NotificationEvent;
import dev.codebymelendez.notifications.domain.model.Notification;
import dev.codebymelendez.notifications.domain.model.NotificationStatus;
import dev.codebymelendez.notifications.domain.repository.NotificationRepository;
import dev.codebymelendez.notifications.infrastructure.messaging.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public Notification createAndQueue(NotificationRequest request) {
        log.info("Creando notificación: channel={}, recipient={}", 
                request.getChannel(), request.getRecipient());

        Notification notification = Notification.builder()
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .metadata(request.getMetadata())
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);
        log.debug("Notificación persistida: id={}", notification.getId());

        NotificationEvent event = NotificationEvent.of(notification.getId());
        notificationPublisher.publish(event);
        log.debug("Evento publicado en RabbitMQ: correlationId={}", event.getCorrelationId());

        notification.markAsQueued();
        notification = notificationRepository.save(notification);
        
        log.info("Notificación encolada exitosamente: id={}, status={}, correlationId={}", 
                notification.getId(), notification.getStatus(), event.getCorrelationId());
        
        return notification;
    }

    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id);
    }

    public Optional<Notification> findByIdWithAttempts(UUID id) {
        return notificationRepository.findByIdWithAttempts(id);
    }
    

    public Page<Notification> findByStatus(NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByStatus(status, pageable);
    }

    public Page<Notification> findAll(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Transactional
    public void markAsDelivered(UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.markAsDelivered();
            notificationRepository.save(notification);
            log.info("Notificación marcada como entregada: id={}", id);
        });
    }

    @Transactional
    public void markAsFailed(UUID id, String reason) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.markAsFailed(reason);
            notificationRepository.save(notification);
            log.warn("Notificación marcada como fallida: id={}, reason={}", id, reason);
        });
    }
}
