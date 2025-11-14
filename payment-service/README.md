# Payment Service

## Purpose
This microservice handles payment processing for orders. It manages payment transactions, validates payment information, processes refunds, and integrates with payment gateways.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Client
- Java 17

## Default Configuration
- Port: 8084
- Service Name: payment-service
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

