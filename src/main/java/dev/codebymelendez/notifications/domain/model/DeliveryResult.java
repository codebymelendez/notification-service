package dev.codebymelendez.notifications.domain.model;


public record DeliveryResult(
        boolean success,
        String errorMessage,
        String errorCode
) {
    
    // ==================== Factory Methods ====================
    
    public static DeliveryResult ok() {
        return new DeliveryResult(true, null, null);
    }
    
    public static DeliveryResult failure(String errorMessage) {
        return new DeliveryResult(false, errorMessage, null);
    }
    
    public static DeliveryResult failure(String errorMessage, String errorCode) {
        return new DeliveryResult(false, errorMessage, errorCode);
    }
    
    public static DeliveryResult fromException(Exception exception) {
        return new DeliveryResult(
                false,
                exception.getMessage(),
                exception.getClass().getSimpleName()
        );
    }
    
    // ==================== Query Methods ====================
    
    public boolean isFailure() {
        return !success;
    }
    
    public boolean hasErrorCode(String code) {
        return errorCode != null && errorCode.equals(code);
    }
    
    public boolean isTransient() {
        if (success || errorCode == null) {
            return false;
        }
        return switch (errorCode) {
            case "TIMEOUT", "CONNECTION_ERROR", "SERVICE_UNAVAILABLE", 
                 "RATE_LIMITED", "TEMPORARY_FAILURE" -> true;
            default -> false;
        };
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public String toString() {
        if (success) {
            return "DeliveryResult{SUCCESS}";
        }
        return String.format("DeliveryResult{FAILED, error='%s', code='%s'}", 
                errorMessage, errorCode);
    }
}
