# Microservices Architecture Project

This repository contains a microservices-based application architecture with multiple independent services.

## Project Structure

```
.
├── eureka-server/          # Service Discovery Server
├── product-service/        # Product Management Service
├── inventory-service/      # Inventory Management Service
├── order-service/          # Order Management Service
└── payment-service/        # Payment Processing Service
```

## Services Overview

### 1. Eureka Server (Port: 8761)
Service discovery server that enables microservices to find and communicate with each other.

### 2. Product Service (Port: 8081)
Manages product catalog, details, pricing, and product-related operations.

### 3. Inventory Service (Port: 8082)
Handles inventory management, stock levels, and product availability tracking.

### 4. Order Service (Port: 8083)
Manages customer orders, order lifecycle, and coordinates with other services for order fulfillment.

### 5. Payment Service (Port: 8084)
Handles payment processing, transaction management, and payment gateway integration.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.1.5
- **Spring Cloud**: 2022.0.4
- **Build Tool**: Maven
- **Service Discovery**: Netflix Eureka

## Prerequisites

- JDK 17 or higher
- Maven 3.6+

## Getting Started

### 1. Start Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```

Wait for Eureka server to start completely (check http://localhost:8761)

### 2. Start Individual Services

Each service can be started independently:

```bash
# Product Service
cd product-service
mvn spring-boot:run

# Inventory Service
cd inventory-service
mvn spring-boot:run

# Order Service
cd order-service
mvn spring-boot:run

# Payment Service
cd payment-service
mvn spring-boot:run
```

### Building All Services

You can build each service individually:

```bash
cd <service-name>
mvn clean install
```

## Service URLs

- Eureka Dashboard: http://localhost:8761
- Product Service: http://localhost:8081
- Inventory Service: http://localhost:8082
- Order Service: http://localhost:8083
- Payment Service: http://localhost:8084

## Next Steps

This is a basic skeleton. You can extend each service with:
- Database integration (JPA, PostgreSQL, MongoDB, etc.)
- REST API endpoints
- Inter-service communication (RestTemplate, WebClient, OpenFeign)
- API Gateway (Spring Cloud Gateway)
- Configuration Server (Spring Cloud Config)
- Circuit breakers (Resilience4j)
- Distributed tracing (Zipkin, Sleuth)
- Message queues (RabbitMQ, Kafka)
- Security (OAuth2, JWT)

## Notes

- Ensure Eureka Server is running before starting other services
- Each service is a standalone Spring Boot application
- Services automatically register with Eureka on startup
- All services use Java 17 and Spring Boot 3.1.5

