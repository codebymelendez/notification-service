package dev.codebymelendez.notifications.api.controller;

import dev.codebymelendez.notifications.api.dto.ApiError;
import dev.codebymelendez.notifications.api.dto.NotificationRequest;
import dev.codebymelendez.notifications.api.dto.NotificationResponse;
import dev.codebymelendez.notifications.api.exception.NotificationNotFoundException;
import dev.codebymelendez.notifications.application.NotificationService;
import dev.codebymelendez.notifications.domain.model.Notification;
import dev.codebymelendez.notifications.domain.model.NotificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API para gestión de notificaciones multicanal")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // ==================== Crear Notificación ====================

    @PostMapping
    @Operation(
        summary = "Enviar notificación",
        description = "Crea y encola una nueva notificación. El procesamiento es asíncrono."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "202",
            description = "Notificación aceptada y encolada",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        
        log.info("Recibida solicitud de notificación: channel={}, recipient={}", 
                request.getChannel(), request.getRecipient());
        
        Notification notification = notificationService.createAndQueue(request);
        
        NotificationResponse response = NotificationResponse.fromEntity(notification);
        
        log.info("Notificación creada y encolada: id={}", notification.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    // ==================== Consultar Notificación ====================

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener notificación por ID",
        description = "Retorna el detalle completo incluyendo historial de intentos de entrega"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Notificación encontrada",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notificación no encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "ID de la notificación", required = true)
            @PathVariable UUID id) {
        
        log.debug("Consultando notificación: id={}", id);
        
        Notification notification = notificationService.findByIdWithAttempts(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        
        NotificationResponse response = NotificationResponse.fromEntityWithAttempts(notification);
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== Listar Notificaciones ====================

    @GetMapping
    @Operation(
        summary = "Listar notificaciones",
        description = "Retorna una lista paginada de notificaciones con filtro opcional por estado"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de notificaciones"
        )
    })
    public ResponseEntity<Page<NotificationResponse>> listNotifications(
            @Parameter(description = "Filtrar por estado")
            @RequestParam(required = false) NotificationStatus status,
            
            @Parameter(description = "Paginación (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        log.debug("Listando notificaciones: status={}, page={}", status, pageable);
        
        Page<Notification> notifications;
        
        if (status != null) {
            notifications = notificationService.findByStatus(status, pageable);
        } else {
            notifications = notificationService.findAll(pageable);
        }
        
        Page<NotificationResponse> response = notifications
                .map(NotificationResponse::fromEntity);
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== Consultar solo Estado ====================

    @GetMapping("/{id}/status")
    @Operation(
        summary = "Obtener estado de notificación",
        description = "Endpoint ligero que retorna solo el estado actual"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estado de la notificación"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notificación no encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<StatusResponse> getNotificationStatus(
            @Parameter(description = "ID de la notificación", required = true)
            @PathVariable UUID id) {
        
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        
        return ResponseEntity.ok(new StatusResponse(
                notification.getId(),
                notification.getStatus(),
                notification.getDeliveredAt(),
                notification.getFailureReason()
        ));
    }
    

    @Schema(description = "Estado simplificado de una notificación")
    public record StatusResponse(
            UUID id,
            NotificationStatus status,
            @Schema(description = "Timestamp de entrega (si aplica)")
            java.time.Instant deliveredAt,
            @Schema(description = "Razón del fallo (si aplica)")
            String failureReason
    ) {}
}
