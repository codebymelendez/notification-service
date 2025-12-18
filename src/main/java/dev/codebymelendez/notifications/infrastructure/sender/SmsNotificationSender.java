package dev.codebymelendez.notifications.infrastructure.sender;

import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.DeliveryResult;
import dev.codebymelendez.notifications.domain.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.regex.Pattern;


@Slf4j
@Component
public class SmsNotificationSender extends AbstractNotificationSender {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+[1-9]\\d{1,14}$"
    );
    
    private static final int SMS_MAX_LENGTH = 160;
    
    @Value("${app.channels.sms.from:+15551234567}")
    private String fromNumber;
    
    @Value("${app.channels.sms.enabled:true}")
    private boolean enabled;
    
    @Value("${app.channels.sms.simulate-failure-rate:5}")
    private int simulateFailureRate;
    
    private final Random random = new Random();
    
    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    protected void validate(Notification notification) {
        super.validate(notification);

        String phone = notification.getRecipient();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException(
                    "Formato de teléfono inválido. Use formato E.164: +[código país][número]. " +
                    "Ejemplo: +34612345678. Recibido: " + phone
            );
        }

        if (notification.getContent().length() > SMS_MAX_LENGTH) {
            log.warn("SMS excede {} caracteres ({}). Se dividirá en múltiples mensajes: notificationId={}", 
                    SMS_MAX_LENGTH, 
                    notification.getContent().length(),
                    notification.getId());
        }
    }
    
    @Override
    protected DeliveryResult doSend(Notification notification) {

        simulateNetworkLatency();

        if (shouldSimulateFailure()) {
            log.warn("[SMS] Simulando fallo de envío para testing");
            return DeliveryResult.failure(
                    "Simulated SMS delivery failure",
                    "SIMULATED_FAILURE"
            );
        }

        int segments = (int) Math.ceil((double) notification.getContent().length() / SMS_MAX_LENGTH);

        log.info("[SMS] SMS enviado (simulado): to={}, from={}, segments={}, chars={}", 
                notification.getRecipient(),
                fromNumber,
                segments,
                notification.getContent().length());
        
        return DeliveryResult.ok();
    }
    
    private void simulateNetworkLatency() {
        try {
            int latency = 200 + random.nextInt(600); // 200-800ms
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean shouldSimulateFailure() {
        if (simulateFailureRate <= 0) {
            return false;
        }
        return random.nextInt(100) < simulateFailureRate;
    }
}
