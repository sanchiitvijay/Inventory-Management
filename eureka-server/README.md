# Eureka Server

## Purpose
This is the service discovery server for the microservices architecture. All microservices register with this Eureka server to enable service-to-service communication and load balancing.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Server
- Java 17

## Default Configuration
- Port: 8761
- Eureka Dashboard: http://localhost:8761

## Running the Application
```bash
mvn spring-boot:run
```

## Build
```bash
mvn clean install
```

