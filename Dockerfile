# jai_websocket/Dockerfile (versión optimizada)
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .
COPY src/ src/

# Cachea dependencias
RUN ./gradlew dependencies --no-daemon

# Build final
RUN ./gradlew bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./app.jar
COPY --from=builder /app/src/main/java/resources/ ./config/

# Salud del contenedor
#HEALTHCHECK --interval=30s --timeout=3s \
#  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:./config/"]