# Product Service

## Purpose
This microservice manages product information including product catalog, details, pricing, and availability. It provides REST APIs for CRUD operations on products.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Client
- Java 17

## Default Configuration
- Port: 8081
- Service Name: product-service
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

