package dev.codebymelendez.notifications.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de error de la API")
public class ApiError {
    
    @Schema(description = "Timestamp del error", example = "2025-01-15T10:30:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    @Schema(description = "Código de estado HTTP", example = "400")
    private int status;
    
    @Schema(description = "Nombre del error HTTP", example = "Bad Request")
    private String error;
    
    @Schema(description = "Mensaje descriptivo del error", example = "Error de validación en los datos de entrada")
    private String message;
    
    @Schema(description = "Path de la solicitud que causó el error", example = "/api/v1/notifications")
    private String path;
    
    @Schema(description = "Código de error interno para tracking", example = "VALIDATION_ERROR")
    private String code;
    
    @Schema(description = "Detalles adicionales del error (ej: errores de validación por campo)")
    private Map<String, String> details;
    
    @Schema(description = "Lista de errores (para múltiples errores)")
    private List<String> errors;
    
    // ==================== Factory Methods ====================

    public static ApiError of(int status, String error, String message, String path) {
        return ApiError.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
    
    public static ApiError validationError(String path, Map<String, String> fieldErrors) {
        return ApiError.builder()
                .status(400)
                .error("Bad Request")
                .message("Error de validación en los datos de entrada")
                .code("VALIDATION_ERROR")
                .path(path)
                .details(fieldErrors)
                .build();
    }
    
    public static ApiError notFound(String path, String message) {
        return ApiError.builder()
                .status(404)
                .error("Not Found")
                .message(message)
                .code("RESOURCE_NOT_FOUND")
                .path(path)
                .build();
    }

    public static ApiError internalError(String path, String message) {
        return ApiError.builder()
                .status(500)
                .error("Internal Server Error")
                .message(message)
                .code("INTERNAL_ERROR")
                .path(path)
                .build();
    }
}
