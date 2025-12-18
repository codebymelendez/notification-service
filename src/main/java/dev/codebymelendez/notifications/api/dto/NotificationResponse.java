package dev.codebymelendez.notifications.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.codebymelendez.notifications.domain.model.Channel;
import dev.codebymelendez.notifications.domain.model.Notification;
import dev.codebymelendez.notifications.domain.model.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta con información de la notificación")
public class NotificationResponse {
    
    @Schema(description = "Identificador único de la notificación")
    private UUID id;
    
    @Schema(description = "Canal de envío")
    private Channel channel;
    
    @Schema(description = "Destinatario")
    private String recipient;
    
    @Schema(description = "Asunto (si aplica)")
    private String subject;
    
    @Schema(description = "Estado actual de la notificación")
    private NotificationStatus status;
    
    @Schema(description = "Razón del fallo (si status = FAILED)")
    private String failureReason;
    
    @Schema(description = "Metadatos asociados")
    private Map<String, Object> metadata;
    
    @Schema(description = "Fecha de creación")
    private Instant createdAt;
    
    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
    
    @Schema(description = "Fecha de entrega exitosa (si status = DELIVERED)")
    private Instant deliveredAt;
    
    @Schema(description = "Historial de intentos de entrega")
    private List<DeliveryAttemptResponse> attempts;
    
    @Schema(description = "URL para consultar el estado de la notificación")
    private String trackingUrl;
    
    // ==================== Factory Methods ====================

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .channel(notification.getChannel())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .failureReason(notification.getFailureReason())
                .metadata(notification.getMetadata())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .deliveredAt(notification.getDeliveredAt())
                .trackingUrl("/api/v1/notifications/" + notification.getId())
                .build();
    }

    public static NotificationResponse fromEntityWithAttempts(Notification notification) {
        NotificationResponse response = fromEntity(notification);
        
        if (notification.getDeliveryAttempts() != null) {
            response.setAttempts(
                notification.getDeliveryAttempts().stream()
                    .map(DeliveryAttemptResponse::fromEntity)
                    .toList()
            );
        }
        
        return response;
    }
}
