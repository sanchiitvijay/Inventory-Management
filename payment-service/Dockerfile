# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/payment-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
