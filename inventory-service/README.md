# Inventory Service

## Purpose
This microservice manages inventory and stock levels for products. It tracks product quantities, handles stock updates, and provides APIs to check product availability.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Client
- Java 17

## Default Configuration
- Port: 8082
- Service Name: inventory-service
- Eureka Server: http://localhost:8761/eureka/

## Running the Application
```bash
mvn spring-boot:run
```

## Build
```bash
mvn clean install
```

## Notes
Ensure the Eureka server is running before starting this service.

