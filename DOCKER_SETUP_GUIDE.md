# Docker Setup Guide

## Overview
Complete Docker containerization for the microservices architecture with Docker Compose for local development.

---

## 📦 Dockerfiles

All services use a standardized multi-stage Docker build:

### Build Pattern
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE <PORT>
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

### Service Dockerfiles

| Service | Location | Port | Image Base |
|---------|----------|------|------------|
| **eureka-server** | `eureka-server/Dockerfile` | 8761 | openjdk:17-jdk-slim |
| **product-service** | `product-service/Dockerfile` | 8081 | openjdk:17-jdk-slim |
| **inventory-service** | `inventory-service/Dockerfile` | 8082 | openjdk:17-jdk-slim |
| **order-service** | `order-service/Dockerfile` | 8083 | openjdk:17-jdk-slim |
| **payment-service** | `payment-service/Dockerfile` | 8084 | openjdk:17-jdk-slim |

---

## 🐳 Docker Compose Configuration

### Services Architecture

```
┌─────────────────────────────────────────────────────┐
│          microservices-network (bridge)             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────┐                               │
│  │ eureka-server   │ :8761 (exposed)               │
│  │   (registry)    │                               │
│  └────────┬────────┘                               │
│           │                                         │
│  ┌────────┼────────────────────────────┐           │
│  │        │                            │           │
│  ▼        ▼                            ▼           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │product-service│  │inventory-svc│  │payment-sv│ │
│  │    :8081     │  │    :8082     │  │  :8084   │ │
│  └──────────────┘  └──────────────┘  └──────────┘ │
│         │                 │                │       │
│         └─────────────────┼────────────────┘       │
│                           │                        │
│                           ▼                        │
│                  ┌──────────────────┐              │
│                  │  order-service   │              │
│                  │  :8083 (exposed) │              │
│                  │  /dashboard      │              │
│                  └──────────────────┘              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Network Configuration

**Network Name:** `microservices-network`  
**Driver:** bridge  
**Purpose:** Allows services to communicate by service name

### Port Mappings

| Service | Internal Port | External Port | Purpose |
|---------|---------------|---------------|---------|
| eureka-server | 8761 | 8761 | Eureka UI & Registry |
| product-service | 8081 | 8081 | Product API |
| inventory-service | 8082 | 8082 | Inventory API |
| order-service | 8083 | 8083 | Order API & Dashboard |
| payment-service | 8084 | 8084 | Payment API |

### Key Features

✅ **Service Discovery:** All services register with Eureka  
✅ **Health Checks:** Eureka has health check before other services start  
✅ **Dependency Management:** Services wait for dependencies  
✅ **Auto-restart:** Services restart on failure  
✅ **Network Isolation:** All services on dedicated bridge network  
✅ **Build Contexts:** Each service builds from its own directory  

---

## 🚀 Quick Start

### Prerequisites
- Docker installed (version 20.10+)
- Docker Compose installed (version 2.0+)
- At least 4GB RAM available for Docker

### Starting All Services

```bash
# From project root directory
docker-compose up --build
```

**What happens:**
1. Builds all 5 Docker images
2. Creates `microservices-network` network
3. Starts eureka-server first (with health check)
4. Starts other services once Eureka is healthy
5. All services register with Eureka

### Starting Services in Background

```bash
docker-compose up -d --build
```

### Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f order-service

# Last 100 lines
docker-compose logs --tail=100 -f
```

### Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Stop without removing containers
docker-compose stop
```

### Rebuilding Specific Service

```bash
# Rebuild and restart single service
docker-compose up -d --build order-service

# Force rebuild (no cache)
docker-compose build --no-cache order-service
docker-compose up -d order-service
```

---

## 🔍 Accessing Services

### URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Eureka Dashboard** | http://localhost:8761 | View all registered services |
| **Order Dashboard** | http://localhost:8083/dashboard | Order statistics & metrics |
| **Product API** | http://localhost:8081/api/products | Product endpoints |
| **Inventory API** | http://localhost:8082/api/inventory | Inventory endpoints |
| **Order API** | http://localhost:8083/api/orders | Order endpoints |
| **Payment API** | http://localhost:8084/api/payments | Payment endpoints |

### Health Checks

```bash
# Check if all services are running
docker-compose ps

# Check Eureka registered services
curl http://localhost:8761/eureka/apps

# Check individual service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

---

## 🧪 Testing the Setup

### 1. Verify Eureka Registration

Wait 30-60 seconds after startup, then visit:
```
http://localhost:8761
```

You should see all 4 services registered:
- PRODUCT-SERVICE
- INVENTORY-SERVICE
- ORDER-SERVICE
- PAYMENT-SERVICE

### 2. Test Service Communication

```bash
# Create a product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-001",
    "description": "Test product for Docker",
    "recommendedRetailPrice": 99.99
  }'

# Add inventory
curl -X POST http://localhost:8082/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "TEST-001",
    "available": 100,
    "threshold": 10
  }'

# Create an order (tests inter-service communication)
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productSku": "TEST-001",
        "quantity": 5,
        "price": 99.99
      }
    ]
  }'

# View dashboard
open http://localhost:8083/dashboard
```

### 3. Test Service Discovery

Services should communicate using service names (e.g., `http://inventory-service:8082`).

Check logs:
```bash
docker-compose logs order-service | grep inventory-service
```

---

## 🔧 Troubleshooting

### Services Not Starting

**Issue:** Container exits immediately  
**Solution:**
```bash
# Check logs
docker-compose logs <service-name>

# Rebuild without cache
docker-compose build --no-cache <service-name>
docker-compose up <service-name>
```

### Eureka Health Check Failing

**Issue:** Services waiting indefinitely for Eureka  
**Solution:**
```bash
# Check Eureka logs
docker-compose logs eureka-server

# Manually verify Eureka is up
curl http://localhost:8761/actuator/health

# If timeout issue, increase start_period in docker-compose.yml
```

### Services Not Registering with Eureka

**Issue:** Services running but not visible in Eureka  
**Solution:**
- Verify network connectivity: `docker network inspect microservices-network`
- Check service logs for registration errors
- Ensure `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` is correct
- Wait 30 seconds for registration to complete

### Port Already in Use

**Issue:** `Error: bind: address already in use`  
**Solution:**
```bash
# Find and stop process using the port
lsof -i :8761  # or other port
kill -9 <PID>

# Or change port mapping in docker-compose.yml
ports:
  - "18761:8761"  # Use external port 18761 instead
```

### Out of Memory

**Issue:** Services keep restarting  
**Solution:**
```bash
# Increase Docker memory limit (Docker Desktop)
# Settings > Resources > Memory > Set to at least 4GB

# Or add memory limits to docker-compose.yml
services:
  order-service:
    deploy:
      resources:
        limits:
          memory: 512M
```

### Inter-Service Communication Failing

**Issue:** Services can't reach each other  
**Solution:**
```bash
# Verify network exists
docker network ls | grep microservices

# Check if all services on same network
docker network inspect microservices-network

# Test connectivity from one container to another
docker exec order-service ping inventory-service
```

---

## 🛠️ Development Workflow

### Making Code Changes

1. **Modify code** in your local editor
2. **Rebuild the service:**
   ```bash
   docker-compose up -d --build <service-name>
   ```
3. **Check logs:**
   ```bash
   docker-compose logs -f <service-name>
   ```

### Hot Reload (Not Enabled)

Current setup requires rebuilding after code changes. For hot reload:
- Use Spring Boot DevTools locally (not in Docker)
- Or mount source code as volumes (not recommended for build complexity)

### Database Persistence

Current setup uses in-memory H2 databases. Data is lost on container restart.

**To persist data:**
```yaml
# Add to each service in docker-compose.yml
volumes:
  - ./data/<service-name>:/app/data
  
# Update application.yml to use file-based H2
spring:
  datasource:
    url: jdbc:h2:file:/app/data/db
```

---

## 📊 Resource Usage

### Expected Resource Consumption

| Service | Memory | CPU | Startup Time |
|---------|--------|-----|--------------|
| eureka-server | ~300MB | Low | 20-30s |
| product-service | ~350MB | Low | 15-25s |
| inventory-service | ~350MB | Low | 15-25s |
| order-service | ~400MB | Low-Med | 20-30s |
| payment-service | ~350MB | Low | 15-25s |
| **Total** | **~1.75GB** | **Low** | **60-90s** |

---

## 🔐 Security Considerations

### Current Setup (Development Only)

⚠️ **Not production-ready!** Current configuration is for local development only.

**Issues:**
- No authentication/authorization
- No HTTPS/TLS
- Default H2 console enabled
- No secrets management
- Exposed ports on all interfaces

### Production Recommendations

1. **Add Spring Security** to all services
2. **Use external databases** (PostgreSQL, MySQL)
3. **Enable HTTPS** with certificates
4. **Use Docker secrets** for sensitive data
5. **Implement API Gateway** (Spring Cloud Gateway)
6. **Add rate limiting** and circuit breakers
7. **Use container scanning** (Trivy, Snyk)

---

## 📝 Build Optimization

### Current Build Time
- First build: ~5-10 minutes (downloads dependencies)
- Subsequent builds: ~2-3 minutes (with cache)

### Optimization Tips

**1. Use Maven Layer Caching:**
```dockerfile
# Download dependencies separately
COPY pom.xml .
RUN mvn dependency:go-offline

# Then copy source
COPY src ./src
RUN mvn clean package -DskipTests
```

**2. Use BuildKit:**
```bash
DOCKER_BUILDKIT=1 docker-compose build
```

**3. Parallel Builds:**
```bash
docker-compose build --parallel
```

---

## 🎯 Next Steps

### Recommended Enhancements

1. **Add API Gateway** (Spring Cloud Gateway)
2. **Add Config Server** for centralized configuration
3. **Add Distributed Tracing** (Zipkin/Jaeger)
4. **Add Monitoring** (Prometheus + Grafana)
5. **Add Message Queue** (RabbitMQ/Kafka)
6. **Add Caching** (Redis)
7. **Add Load Balancer** (Nginx/Traefik)

---

## 📦 Docker Commands Reference

```bash
# Build
docker-compose build                    # Build all services
docker-compose build --no-cache        # Build without cache
docker-compose build --parallel        # Build in parallel

# Start
docker-compose up                      # Start (foreground)
docker-compose up -d                   # Start (background)
docker-compose up --build              # Build and start
docker-compose up --scale order-service=3  # Scale service

# Stop
docker-compose stop                    # Stop services
docker-compose down                    # Stop and remove
docker-compose down -v                 # Stop, remove, and delete volumes

# Logs
docker-compose logs                    # All logs
docker-compose logs -f                 # Follow logs
docker-compose logs --tail=100 -f      # Last 100 lines
docker-compose logs <service>          # Specific service

# Status
docker-compose ps                      # List containers
docker-compose top                     # Show running processes

# Execute Commands
docker-compose exec <service> bash     # Shell access
docker-compose exec <service> curl localhost:8081  # Run command

# Clean Up
docker-compose down --rmi all          # Remove images too
docker system prune -a                 # Clean everything (careful!)
```

---

## 📄 Summary

✅ **5 Dockerfiles Created** - Standardized multi-stage builds  
✅ **docker-compose.yml** - Complete orchestration for local dev  
✅ **Bridge Network** - Services communicate by name  
✅ **Health Checks** - Proper startup order  
✅ **Port Exposure** - Eureka (8761) and Order Dashboard (8083)  
✅ **Build Contexts** - Each service builds independently  
✅ **Auto-restart** - Resilient to failures  

**Everything is ready for local Docker development! 🚀**
