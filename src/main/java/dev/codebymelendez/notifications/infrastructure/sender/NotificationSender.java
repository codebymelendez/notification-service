package dev.codebymelendez.notifications.infrastructure.sender;

import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.DeliveryResult;
import dev.codebymelendez.notifications.domain.model.Notification;


public interface NotificationSender {

    Channel getChannel();
    
    DeliveryResult send(Notification notification);
    
    default boolean supports(Channel channel) {
        return getChannel() == channel;
    }
    
    default boolean isEnabled() {
        return true;
    }
}
