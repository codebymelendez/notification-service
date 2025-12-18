-- =====================================================
-- V1: Crear tablas del sistema de notificaciones
-- =====================================================

-- Tabla principal de notificaciones
CREATE TABLE notifications (
    id              UUID PRIMARY KEY,
    channel         VARCHAR(20) NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(500),
    content         TEXT NOT NULL,
    metadata        JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason  TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    delivered_at    TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'SMS', 'CONSOLE')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'QUEUED', 'PROCESSING', 'DELIVERED', 'FAILED'))
);

-- Tabla de intentos de entrega (historial)
CREATE TABLE delivery_attempts (
    id                  UUID PRIMARY KEY,
    notification_id     UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    attempt_number      INT NOT NULL,
    status              VARCHAR(20) NOT NULL,
    error_message       TEXT,
    attempted_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_attempt_status CHECK (status IN ('SUCCESS', 'FAILED')),
    CONSTRAINT uq_notification_attempt UNIQUE (notification_id, attempt_number)
);

-- =====================================================
-- Índices para optimizar consultas frecuentes
-- =====================================================

-- Buscar notificaciones por estado (para monitoring/dashboards)
CREATE INDEX idx_notifications_status ON notifications(status);

-- Buscar notificaciones por canal y estado
CREATE INDEX idx_notifications_channel_status ON notifications(channel, status);

-- Buscar notificaciones recientes
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Buscar intentos por notificación
CREATE INDEX idx_delivery_attempts_notification ON delivery_attempts(notification_id);

-- =====================================================
-- Función para actualizar updated_at automáticamente
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Comentarios de documentación
-- =====================================================
COMMENT ON TABLE notifications IS 'Tabla principal que almacena todas las notificaciones del sistema';
COMMENT ON TABLE delivery_attempts IS 'Historial de intentos de entrega para cada notificación';
COMMENT ON COLUMN notifications.metadata IS 'Datos adicionales en formato JSON (orderId, priority, etc.)';
COMMENT ON COLUMN notifications.channel IS 'Canal de entrega: EMAIL, SMS, CONSOLE';
COMMENT ON COLUMN notifications.status IS 'Estado del ciclo de vida: PENDING → QUEUED → PROCESSING → DELIVERED/FAILED';
