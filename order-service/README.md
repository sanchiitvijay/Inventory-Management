# Order Service

## Purpose
This microservice manages customer orders. It handles order creation, updates, cancellations, and tracks order status. It typically communicates with product, inventory, and payment services to complete order processing.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Client
- Java 17

## Default Configuration
- Port: 8083
- Service Name: order-service
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

