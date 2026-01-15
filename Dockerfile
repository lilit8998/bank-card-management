# --- Build Stage ---
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM openjdk:17-jre-slim
WORKDIR /app
RUN addgroup --system spring && adduser --system spring --ingroup spring
COPY --from=build /app/target/*.jar app.jar
RUN chown -R spring:spring /app
USER spring:spring
EXPOSE 7404
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:7404/api/actuator/health || exit 1
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
