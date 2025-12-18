package dev.codebymelendez.notifications.infrastructure.sender;

import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.DeliveryResult;
import dev.codebymelendez.notifications.domain.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ConsoleNotificationSender extends AbstractNotificationSender {
    
    @Override
    public Channel getChannel() {
        return Channel.CONSOLE;
    }
    
    @Override
    protected DeliveryResult doSend(Notification notification) {
        String separator = "═".repeat(60);
        
        StringBuilder message = new StringBuilder();
        message.append("\n").append(separator);
        message.append("\n📬 NOTIFICACIÓN [CONSOLE]");
        message.append("\n").append(separator);
        message.append("\n  ID:           ").append(notification.getId());
        message.append("\n  Destinatario: ").append(notification.getRecipient());
        
        if (notification.getSubject() != null && !notification.getSubject().isBlank()) {
            message.append("\n  Asunto:       ").append(notification.getSubject());
        }
        
        message.append("\n  ─────────────────────────────────────────────────────────");
        message.append("\n  Contenido:");
        message.append("\n  ").append(notification.getContent().replace("\n", "\n  "));
        message.append("\n").append(separator);
        
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            message.append("\n  Metadata: ").append(notification.getMetadata());
            message.append("\n").append(separator);
        }

        log.info(message.toString());

        return DeliveryResult.ok();
    }
}
