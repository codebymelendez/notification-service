package dev.codebymelendez.notifications.domain.model;


import lombok.Getter;

@Getter
public enum NotificationStatus {
    
    PENDING("pending", "Pendiente", false),
    
    QUEUED("queued", "En Cola", false),
    
    PROCESSING("processing", "Procesando", false),
    
    DELIVERED("delivered", "Entregada", true),
    
    FAILED("failed", "Fallida", true);
    
    private final String code;
    private final String displayName;
    private final boolean terminal;
    
    NotificationStatus(String code, String displayName, boolean terminal) {
        this.code = code;
        this.displayName = displayName;
        this.terminal = terminal;
    }

    public boolean isSuccessful() {
        return this == DELIVERED;
    }
    
    public boolean canBeProcessed() {
        return !terminal;
    }
}
