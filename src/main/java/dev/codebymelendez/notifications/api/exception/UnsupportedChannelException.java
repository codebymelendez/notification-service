package dev.codebymelendez.notifications.api.exception;

import dev.codebymelendez.notifications.domain.model.Channel;


public class UnsupportedChannelException extends NotificationException {
    
    private final Channel channel;
    
    public UnsupportedChannelException(Channel channel) {
        super(
            String.format("Canal no soportado o deshabilitado: %s", channel),
            "UNSUPPORTED_CHANNEL"
        );
        this.channel = channel;
    }
    
    public Channel getChannel() {
        return channel;
    }
}
