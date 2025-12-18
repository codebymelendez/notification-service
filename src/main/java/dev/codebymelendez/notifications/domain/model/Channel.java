package dev.codebymelendez.notifications.domain.model;


public enum Channel {
    
    EMAIL("email", "Correo Electrónico"),
    
    SMS("sms", "Mensaje de Texto"),
    
    CONSOLE("console", "Consola (Debug)");
    
    private final String code;
    private final String displayName;
    
    Channel(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static Channel fromCode(String code) {
        for (Channel channel : values()) {
            if (channel.code.equalsIgnoreCase(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Canal desconocido: " + code);
    }
}
