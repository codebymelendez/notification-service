package dev.codebymelendez.notifications.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Sistema de Notificaciones Multicanal - API REST para envío de notificaciones a través de múltiples canales (Email, SMS, Console) con procesamiento asíncrono y reintentos automáticos.")
                        .version("1.0.1")
                        .contact(new Contact()
                                .name("David Melendez")
                                .email("david@codebymelendez.com")
                                .url("https://codebymelendez.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
