# 🔔 Notification Service

Sistema de notificaciones multicanal con arquitectura **Event-Driven**, diseñado para demostrar buenas prácticas de ingeniería de software y patrones de diseño empresariales.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

## 📋 Descripción

Este servicio permite enviar notificaciones a través de múltiples canales (Email, SMS, Console) de forma asíncrona y resiliente. Está diseñado siguiendo principios SOLID y patrones de diseño que facilitan la extensibilidad y el mantenimiento.

### Problema que Resuelve

Las empresas necesitan comunicarse con sus clientes por múltiples canales de forma:
- **Unificada**: Una sola API para todos los canales
- **Resiliente**: Reintentos automáticos ante fallos temporales
- **Trazable**: Historial completo de cada notificación
- **Extensible**: Agregar nuevos canales sin modificar código existente

## ✨ Características Técnicas

| Característica | Implementación |
|----------------|----------------|
| Procesamiento Asíncrono | RabbitMQ con consumers dedicados |
| Extensibilidad | Strategy Pattern para canales |
| Resiliencia | Retry con backoff exponencial + DLQ |
| Persistencia | PostgreSQL con Flyway migrations |
| Testing | Testcontainers para tests de integración |
| Documentación | OpenAPI/Swagger UI |
| Containerización | Docker multi-stage build |

## 🏗️ Arquitectura

```
┌─────────────┐     ┌──────────────────────────────────────────┐
│   Cliente   │────▶│         NOTIFICATION SERVICE             │
│  (REST API) │     │                                          │
└─────────────┘     │  ┌────────────┐    ┌─────────────────┐   │
                    │  │ Controller │───▶│ NotificationSvc │   │
                    │  └────────────┘    └────────┬────────┘   │
                    │                             │            │
                    │                    ┌────────▼────────┐   │
                    │                    │   PostgreSQL    │   │
                    │                    │   (persist)     │   │
                    │                    └────────┬────────┘   │
                    │                             │            │
                    │                    ┌────────▼────────┐   │
                    │                    │    RabbitMQ     │   │
                    │                    │   (async pub)   │   │
                    │                    └────────┬────────┘   │
                    │         ┌───────────────────┼───────┐    │
                    │         ▼                   ▼       ▼    │
                    │    ┌─────────┐      ┌───────┐ ┌───────┐  │
                    │    │  Email  │      │  SMS  │ │Console│  │
                    │    │ Sender  │      │Sender │ │Sender │  │
                    │    └─────────┘      └───────┘ └───────┘  │
                    └──────────────────────────────────────────┘
```

## 🎯 Patrones de Diseño

### Strategy Pattern
Cada canal de notificación implementa la interfaz `NotificationSender`, permitiendo agregar nuevos canales sin modificar el código existente (Open/Closed Principle).

```java
public interface NotificationSender {
    Channel getChannel();
    DeliveryResult send(Notification notification);
}
```

### Template Method
`AbstractNotificationSender` define el flujo común (validación → envío → logging) mientras cada implementación concreta solo define `doSend()`.

### Outbox Pattern (Simplificado)
Las notificaciones se persisten primero en la base de datos antes de publicarse en RabbitMQ, garantizando consistencia eventual.

### Dead Letter Queue
Mensajes que fallan después de N intentos se mueven automáticamente a una DLQ para análisis y reprocesamiento manual.

## 🚀 Inicio Rápido

### Prerrequisitos
- Docker y Docker Compose
- Java 23 (para desarrollo local)
- Maven 3.9+

### Levantar con Docker

```bash
# Clonar el repositorio
git clone https://github.com/codebymelendez/notification-service.git
cd notification-service

# Levantar infraestructura
docker-compose up -d postgres rabbitmq

# Esperar a que estén healthy
docker-compose ps

# Compilar y ejecutar
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Probar la API

```bash
# Enviar notificación por consola (para pruebas)
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "CONSOLE",
    "recipient": "test@example.com",
    "content": "¡Hola! Esta es una notificación de prueba."
  }'

# Consultar estado
curl http://localhost:8080/api/v1/notifications/{id}
```

### Documentación API
Una vez levantada la aplicación: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 📁 Estructura del Proyecto

```
notification-service/
├── src/main/java/.../notifications/
│   ├── api/                    # Capa de presentación (REST)
│   │   ├── controller/
│   │   ├── dto/
│   │   └── exception/
│   ├── application/            # Casos de uso / Servicios
│   ├── domain/                 # Entidades y repositorios
│   ├── infrastructure/         # Implementaciones técnicas
│   │   ├── messaging/          # RabbitMQ publisher/consumer
│   │   └── sender/             # Strategy implementations
│   └── config/                 # Configuración de Spring
```

## 📈 Mejoras Futuras

- [ ] Métricas con Micrometer + Prometheus + Grafana
- [ ] Rate limiting por cliente
- [ ] Priorización de mensajes (colas prioritarias)
- [ ] Templates de mensajes (Thymeleaf)
- [ ] Programación de envíos (delayed messages)
- [ ] Dashboard de monitoreo (React)


## 👤 Autor

**David Melendez**  
Senior Software Engineer | Backend Specialist

- Portfolio: [codebymelendez.com](https://codebymelendez.com)
- LinkedIn: [linkedin.com/in/ramonmelendezjuarez](https://linkedin.com/in/ramonmelendezjuarez)
- GitHub: [github.com/dmelendez](https://github.com/codebymelendez)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.
