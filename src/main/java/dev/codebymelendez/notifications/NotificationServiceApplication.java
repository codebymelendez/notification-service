package dev.codebymelendez.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Notification Service - Sistema de Notificaciones Multicanal
 * 
 * <p>Arquitectura Event-Driven con soporte para múltiples canales
 * 
 * @author Ramon Melendez
 * @see <a href="https://codebymelendez.com">codebymelendez.com</a>
 */
@SpringBootApplication
@EnableRetry
public class NotificationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
