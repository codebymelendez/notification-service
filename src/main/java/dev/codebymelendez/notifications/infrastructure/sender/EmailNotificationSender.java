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
public class EmailNotificationSender extends AbstractNotificationSender {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @Value("${app.channels.email.from:noreply@codebymelendez.dev}")
    private String fromAddress;
    
    @Value("${app.channels.email.enabled:true}")
    private boolean enabled;
    
    @Value("${app.channels.email.simulate-failure-rate:10}")
    private int simulateFailureRate;
    
    private final Random random = new Random();
    
    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    protected void validate(Notification notification) {
        super.validate(notification);
        String email = notification.getRecipient();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Formato de email inválido: " + email
            );
        }

        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            log.warn("Email sin asunto: notificationId={}", notification.getId());
        }
    }
    
    @Override
    protected DeliveryResult doSend(Notification notification) {

        simulateNetworkLatency();

        if (shouldSimulateFailure()) {
            log.warn("[EMAIL] Simulando fallo de envío para testing");
            return DeliveryResult.failure(
                    "Simulated email delivery failure",
                    "SIMULATED_FAILURE"
            );
        }

        log.info("[EMAIL] Email enviado (simulado): to={}, subject={}, from={}", 
                notification.getRecipient(),
                notification.getSubject(),
                fromAddress);
        
        return DeliveryResult.ok();
    }
    
    private void simulateNetworkLatency() {
        try {
            int latency = 100 + random.nextInt(400); // 100-500ms
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
