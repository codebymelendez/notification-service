package dev.codebymelendez.notifications.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.codebymelendez.notifications.domain.model.DeliveryAttempt;
import dev.codebymelendez.notifications.domain.model.DeliveryAttemptStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Información de un intento de entrega")
public class DeliveryAttemptResponse {
    
    @Schema(description = "Número secuencial del intento (1, 2, 3...)", example = "1")
    private int attemptNumber;
    
    @Schema(description = "Resultado del intento", example = "SUCCESS")
    private DeliveryAttemptStatus status;
    
    @Schema(description = "Mensaje de error (si falló)")
    private String errorMessage;
    
    @Schema(description = "Timestamp del intento")
    private Instant timestamp;
    

    public static DeliveryAttemptResponse fromEntity(DeliveryAttempt attempt) {
        return DeliveryAttemptResponse.builder()
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus())
                .errorMessage(attempt.getErrorMessage())
                .timestamp(attempt.getAttemptedAt())
                .build();
    }
}
