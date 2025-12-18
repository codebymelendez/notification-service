package dev.codebymelendez.notifications.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private Channel channel;
    
    @Column(name = "recipient", nullable = false)
    private String recipient;
    
    @Column(name = "subject", length = 500)
    private String subject;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("attemptNumber ASC")
    @Builder.Default
    private List<DeliveryAttempt> deliveryAttempts = new ArrayList<>();
    
    // ==================== Lifecycle Callbacks ====================
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // ==================== Business Methods ====================
    
    public void markAsQueued() {
        if (this.status != NotificationStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se puede encolar una notificación en estado PENDING. Estado actual: " + this.status
            );
        }
        this.status = NotificationStatus.QUEUED;
    }
    
    public void markAsProcessing() {
        if (this.status != NotificationStatus.QUEUED) {
            throw new IllegalStateException(
                "Solo se puede procesar una notificación en estado QUEUED. Estado actual: " + this.status
            );
        }
        this.status = NotificationStatus.PROCESSING;
    }
    
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
    }
    
    public DeliveryAttempt recordAttempt(boolean success, String errorMessage) {
        int attemptNumber = this.deliveryAttempts.size() + 1;
        
        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notification(this)
                .attemptNumber(attemptNumber)
                .status(success ? DeliveryAttemptStatus.SUCCESS : DeliveryAttemptStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
        
        this.deliveryAttempts.add(attempt);
        return attempt;
    }
    
    public int getAttemptCount() {
        return this.deliveryAttempts.size();
    }
    
    public boolean canRetry(int maxAttempts) {
        return !this.status.isTerminal() && getAttemptCount() < maxAttempts;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public String toString() {
        return String.format(
            "Notification{id=%s, channel=%s, recipient='%s', status=%s, attempts=%d}",
            id, channel, recipient, status, getAttemptCount()
        );
    }
}
