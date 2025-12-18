package dev.codebymelendez.notifications.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
    name = "delivery_attempts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_notification_attempt",
            columnNames = {"notification_id", "attempt_number"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAttempt {
    

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryAttemptStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;
    
    // ==================== Lifecycle Callbacks ====================
    
    @PrePersist
    protected void onCreate() {
        if (this.attemptedAt == null) {
            this.attemptedAt = Instant.now();
        }
    }
    
    // ==================== Business Methods ====================
    
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }
    
    public boolean isRetry() {
        return attemptNumber > 1;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public String toString() {
        return String.format(
            "DeliveryAttempt{notificationId=%s, attempt=%d, status=%s, error='%s'}",
            notification != null ? notification.getId() : "null",
            attemptNumber,
            status,
            errorMessage != null ? errorMessage.substring(0, Math.min(50, errorMessage.length())) : "null"
        );
    }
}
