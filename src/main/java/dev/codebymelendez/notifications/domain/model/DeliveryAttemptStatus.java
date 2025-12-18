package dev.codebymelendez.notifications.domain.model;


public enum DeliveryAttemptStatus {
    
    SUCCESS("success", "Exitoso"),

    FAILED("failed", "Fallido");
    
    private final String code;
    private final String displayName;
    
    DeliveryAttemptStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isSuccessful() {
        return this == SUCCESS;
    }
}
