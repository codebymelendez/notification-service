package dev.codebymelendez.notifications.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private UUID notificationId;

    @Builder.Default
    private Instant createdAt = Instant.now();
    

    @Builder.Default
    private int attemptNumber = 1;

    private String correlationId;
    
    // ==================== Factory Methods ====================

    public static NotificationEvent of(UUID notificationId) {
        return NotificationEvent.builder()
                .notificationId(notificationId)
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    public NotificationEvent forRetry() {
        return NotificationEvent.builder()
                .notificationId(this.notificationId)
                .createdAt(Instant.now())
                .attemptNumber(this.attemptNumber + 1)
                .correlationId(this.correlationId)
                .build();
    }
    
    // ==================== Query Methods ====================

    public boolean isRetry() {
        return attemptNumber > 1;
    }
    
    @Override
    public String toString() {
        return String.format(
            "NotificationEvent{notificationId=%s, attempt=%d, correlationId=%s}",
            notificationId, attemptNumber, correlationId
        );
    }
}
