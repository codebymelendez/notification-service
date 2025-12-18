package dev.codebymelendez.notifications.infrastructure.messaging;

import dev.codebymelendez.notifications.application.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange.notifications}")
    private String exchange;
    
    @Value("${app.rabbitmq.routing-key.notifications}")
    private String routingKey;
    
    public void publish(NotificationEvent event) {
        log.info("Publicando evento: notificationId={}, attempt={}, correlationId={}", 
                event.getNotificationId(), 
                event.getAttemptNumber(),
                event.getCorrelationId());
        
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            
            log.debug("Evento publicado exitosamente en exchange={}, routingKey={}", 
                    exchange, routingKey);
            
        } catch (Exception e) {
            log.error("Error al publicar evento: notificationId={}, error={}", 
                    event.getNotificationId(), e.getMessage(), e);
            throw new RuntimeException("Error al publicar notificación en RabbitMQ", e);
        }
    }
    
    public void publish(UUID notificationId) {
        NotificationEvent event = NotificationEvent.of(notificationId);
        publish(event);
    }
    
    public void publishRetry(NotificationEvent originalEvent) {
        NotificationEvent retryEvent = originalEvent.forRetry();
        
        log.info("Publicando reintento: notificationId={}, attempt={}", 
                retryEvent.getNotificationId(), 
                retryEvent.getAttemptNumber());
        
        publish(retryEvent);
    }
}
