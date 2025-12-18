# =====================================================
# NOTIFICATION SERVICE - Dockerfile
# =====================================================
# Multi-stage build para imagen optimizada
# 
# Build:  docker build -t notification-service .
# Run:    docker run -p 8080:8080 notification-service

# ==================== Stage 1: Build ====================
FROM maven:3.9-eclipse-temurin-23-alpine AS builder

WORKDIR /build

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -B

# ==================== Stage 2: Runtime ====================
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar JAR desde el builder
COPY --from=builder /build/target/*.jar app.jar

# Cambiar a usuario no-root
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Configuración JVM optimizada para containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
