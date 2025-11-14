# Microservices Architecture Project

A production-ready microservices application with service discovery, health monitoring, comprehensive testing (89 tests), and operational dashboard.

## 🏗️ Architecture

```
.
├── eureka-server/          # Service Discovery Server
├── product-service/        # Product Management Service
├── inventory-service/      # Inventory Management Service
├── order-service/          # Order Management Service
└── payment-service/        # Payment Processing Service
```

| Service | Port | Description |
|---------|------|-------------|
| **eureka-server** | 8761 | Service discovery and registry |
| **product-service** | 8081 | Product catalog management |
| **inventory-service** | 8082 | Stock levels and low-stock alerts |
| **order-service** | 8083 | Order management + **Dashboard** |
| **payment-service** | 8084 | Payment processing |

**Tech Stack**: Java 17 • Spring Boot 3.1.5 • Spring Cloud 2022.0.4 • Maven • Netflix Eureka • Docker • Kubernetes

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- JDK 17+
- Maven 3.6+
- Docker & Docker Compose (optional)

### Option 1: Maven (Traditional)

```bash
# Build all services (parallel build)
mvn -T 1C clean package

# Start Eureka first
cd eureka-server && mvn spring-boot:run

# In separate terminals, start other services
cd product-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
```

### Option 2: Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# Or run in background
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

**Wait 60-90 seconds** for services to register with Eureka.

### Option 3: Kubernetes

```bash
# Build Docker images for each service
cd eureka-server
docker build -t <your-registry>/eureka-server:latest .

cd ../product-service
docker build -t <your-registry>/product-service:latest .

cd ../inventory-service
docker build -t <your-registry>/inventory-service:latest .

cd ../order-service
docker build -t <your-registry>/order-service:latest .

cd ../payment-service
docker build -t <your-registry>/payment-service:latest .

# Push to registry
docker push <your-registry>/eureka-server:latest
docker push <your-registry>/product-service:latest
docker push <your-registry>/inventory-service:latest
docker push <your-registry>/order-service:latest
docker push <your-registry>/payment-service:latest

# Update image references in k8s/*.yml files, then deploy
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/
```

**Note**: Update image references in `k8s/*/deployment.yml` to match your registry/tag.

---

## 📊 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Eureka Dashboard** | http://localhost:8761 | View all registered services |
| **Order Dashboard** | http://localhost:8083/dashboard | Operational dashboard (orders, products, inventory) |
| Product API | http://localhost:8081/api/products | REST API for products |
| Inventory API | http://localhost:8082/api/inventory | REST API for inventory |
| Order API | http://localhost:8083/api/orders | REST API for orders |
| Payment API | http://localhost:8084/api/payments | REST API for payments |

### Health Endpoints
All services expose actuator endpoints:
- `/actuator/health` - Overall health status
- `/actuator/health/readiness` - Readiness probe
- `/actuator/health/liveness` - Liveness probe

---

## 🧪 Testing

**89 Total Tests** with comprehensive coverage:

| Service | Tests | Type |
|---------|-------|------|
| product-service | 10 | MockMvc slice tests |
| inventory-service | 50 | Unit + integration + low-stock alerts |
| payment-service | 11 | Deterministic success/fail logic |
| order-service | 18 | Unit + **5 E2E integration tests** |

### Run Tests

```bash
# All services
mvn test

# Specific service
cd <service-name> && mvn test

# E2E integration test only (WireMock-based)
cd order-service && mvn test -Dtest=EndToEndIntegrationTest
```

**E2E Test Scenarios**:
1. Create Product → Set Inventory → Order → Payment SUCCESS → Inventory Deducted → PAID ✅
2. Order → Payment SUCCESS → Insufficient Stock → CANCELLED ✅
3. Order → Payment FAILED → CANCELLED (no inventory deduction) ✅

📖 **Detailed docs**: [TESTING_GUIDE.md](TESTING_GUIDE.md) • [E2E_INTEGRATION_TEST.md](E2E_INTEGRATION_TEST.md)

---

## 🎯 Key Features

### ✅ Service Discovery
- All services auto-register with Eureka
- Dynamic service lookup (no hardcoded URLs in production)

### ✅ Health Monitoring
- Spring Boot Actuator on all services
- Kubernetes readiness/liveness probes configured
- Health checks: `/actuator/health`

### ✅ Low-Stock Alerts
- Automatic alerts when inventory falls below threshold
- Database persistence with timestamps
- REST API: `GET /api/inventory/alerts`

### ✅ Operational Dashboard
- Real-time order statistics by status
- Top products by order count
- Low-stock items tracking
- Service health indicators
- Access at: http://localhost:8083/dashboard

### ✅ Inter-Service Communication
- RestTemplate with load balancing
- Circuit breaker patterns (ready for Resilience4j)
- Retry logic on inventory/payment services

---

## 📁 Project Structure

```
.
├── eureka-server/          # Service registry
├── product-service/        # Product catalog
├── inventory-service/      # Stock management + alerts
├── order-service/          # Orders + dashboard
├── payment-service/        # Payment processing
├── k8s/                    # Kubernetes manifests
├── docker-compose.yml      # Docker Compose setup
└── TESTING_GUIDE.md        # Complete testing docs
```

---

## 🔧 Development Notes

- **Startup Order**: Eureka must start first, then other services (60-90s registration time)
- **Database**: All services use H2 in-memory database (data cleared on restart)
- **Ports**: Default ports are 8761, 8081-8084; configurable in `application.yml`
- **Docker**: Images use multi-stage builds for optimized size
- **Kubernetes**: Configured with 3 replicas per service, ClusterIP services, ConfigMaps

---

## 📚 Documentation

- [TESTING_GUIDE.md](TESTING_GUIDE.md) - All 89 tests documented
- [E2E_INTEGRATION_TEST.md](E2E_INTEGRATION_TEST.md) - End-to-end testing guide
- [k8s/README.md](k8s/README.md) - Kubernetes deployment guide

---

## 🚧 Future Enhancements

- [ ] API Gateway (Spring Cloud Gateway)
- [ ] Configuration Server (Spring Cloud Config)
- [ ] Distributed tracing (Zipkin/Sleuth)
- [ ] Message queues (RabbitMQ/Kafka)
- [ ] Security (OAuth2/JWT)
- [ ] PostgreSQL/MongoDB for production
- [ ] Circuit breakers (Resilience4j)

---

## 📝 License

This project is for educational/demonstration purposes

