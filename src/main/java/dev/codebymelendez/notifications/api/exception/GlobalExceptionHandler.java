package dev.codebymelendez.notifications.api.exception;

import dev.codebymelendez.notifications.api.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // ==================== Excepciones de Dominio ====================

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotificationNotFound(
            NotificationNotFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Notificación no encontrada: {}", ex.getMessage());
        
        ApiError error = ApiError.notFound(
                request.getRequestURI(),
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
   @ExceptionHandler(InvalidNotificationException.class)
    public ResponseEntity<ApiError> handleInvalidNotification(
            InvalidNotificationException ex,
            HttpServletRequest request) {
        
        log.warn("Notificación inválida: {}", ex.getMessage());
        
        ApiError error = ApiError.of(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        error.setCode(ex.getErrorCode());

        if (ex.getField() != null) {
            error.setDetails(Map.of(ex.getField(), ex.getMessage()));
        }
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UnsupportedChannelException.class)
    public ResponseEntity<ApiError> handleUnsupportedChannel(
            UnsupportedChannelException ex,
            HttpServletRequest request) {
        
        log.warn("Canal no soportado: {}", ex.getChannel());
        
        ApiError error = ApiError.of(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        error.setCode(ex.getErrorCode());
        
        return ResponseEntity.badRequest().body(error);
    }
    

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiError> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {
        
        log.error("Error de notificación: {}", ex.getMessage());
        
        ApiError error = ApiError.of(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        error.setCode(ex.getErrorCode());
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // ==================== Excepciones de Validación ====================
    

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        log.warn("Error de validación en {}: {}", request.getRequestURI(), fieldErrors);
        
        ApiError error = ApiError.validationError(request.getRequestURI(), fieldErrors);
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonParseError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        log.warn("Error de parseo JSON: {}", ex.getMessage());
        
        String message = "Error en el formato del JSON. Verifique la estructura del request.";
        if (ex.getMessage() != null && ex.getMessage().contains("Channel")) {
            message = "Canal inválido. Valores permitidos: EMAIL, SMS, CONSOLE";
        }
        
        ApiError error = ApiError.of(
                400,
                "Bad Request",
                message,
                request.getRequestURI()
        );
        error.setCode("INVALID_JSON");
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        log.warn("Error de tipo de argumento: {} = {}", ex.getName(), ex.getValue());
        
        String message = String.format(
                "El parámetro '%s' tiene un formato inválido: '%s'",
                ex.getName(),
                ex.getValue()
        );
        
        ApiError error = ApiError.of(
                400,
                "Bad Request",
                message,
                request.getRequestURI()
        );
        error.setCode("INVALID_PARAMETER");
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // ==================== Excepciones de Estado ====================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {
        
        log.warn("Estado ilegal: {}", ex.getMessage());
        
        ApiError error = ApiError.of(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        error.setCode("INVALID_STATE");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    // ==================== Excepciones Genéricas ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Error interno no manejado en {}: ", request.getRequestURI(), ex);
        ApiError error = ApiError.internalError(
                request.getRequestURI(),
                "Ha ocurrido un error interno. Por favor, contacte al administrador."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
