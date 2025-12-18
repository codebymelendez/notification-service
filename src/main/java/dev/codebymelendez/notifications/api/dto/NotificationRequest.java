package dev.codebymelendez.notifications.api.dto;

import dev.codebymelendez.notifications.domain.model.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * <p>Ejemplo de uso:
 * <pre>
 * {
 *   "channel": "EMAIL",
 *   "recipient": "cliente@example.com",
 *   "subject": "Confirmación de pedido",
 *   "content": "Tu pedido #12345 ha sido confirmado",
 *   "metadata": {
 *     "orderId": "12345",
 *     "priority": "high"
 *   }
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para enviar una notificación")
public class NotificationRequest {
    

    @NotNull(message = "El canal es obligatorio")
    @Schema(
        description = "Canal de envío de la notificación",
        example = "EMAIL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Channel channel;

    @NotBlank(message = "El destinatario es obligatorio")
    @Size(max = 255, message = "El destinatario no puede exceder 255 caracteres")
    @Schema(
        description = "Destinatario (email, teléfono, etc.)",
        example = "cliente@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String recipient;
    

    @Size(max = 500, message = "El asunto no puede exceder 500 caracteres")
    @Schema(
        description = "Asunto (principalmente para EMAIL)",
        example = "Confirmación de pedido #12345"
    )
    private String subject;
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 10000, message = "El contenido no puede exceder 10000 caracteres")
    @Schema(
        description = "Contenido del mensaje",
        example = "Estimado cliente, su pedido ha sido confirmado.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;
    

    @Schema(
        description = "Metadatos adicionales (opcional)"
    )
    private Map<String, Object> metadata;
}
