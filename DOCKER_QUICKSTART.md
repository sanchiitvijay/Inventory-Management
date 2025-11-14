# Docker Quick Start

## 🚀 Start All Services

```bash
docker-compose up --build
```

Wait 60-90 seconds for all services to start and register with Eureka.

---

## 📍 Access Points

| Service | URL |
|---------|-----|
| **Eureka Dashboard** | http://localhost:8761 |
| **Order Dashboard** | http://localhost:8083/dashboard |
| Product API | http://localhost:8081/api/products |
| Inventory API | http://localhost:8082/api/inventory |
| Order API | http://localhost:8083/api/orders |
| Payment API | http://localhost:8084/api/payments |

---

## 🛑 Stop All Services

```bash
docker-compose down
```

---

## 📋 Services

```
eureka-server      :8761  (Service Registry)
product-service    :8081  (Products)
inventory-service  :8082  (Inventory)
order-service      :8083  (Orders + Dashboard)
payment-service    :8084  (Payments)
```

---

## 🔍 View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f order-service
```

---

## 🧪 Quick Test

```bash
# 1. Wait for services to start (check Eureka)
open http://localhost:8761

# 2. View order dashboard
open http://localhost:8083/dashboard

# 3. Create test order
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productSku": "TEST-001", "quantity": 5, "price": 29.99}
    ]
  }'
```

---

## 🏗️ Architecture

```
                    ┌─────────────────┐
                    │ eureka-server   │
                    │     :8761       │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
    ┌──────────┐      ┌──────────┐      ┌──────────┐
    │ product  │      │inventory │      │ payment  │
    │  :8081   │      │  :8082   │      │  :8084   │
    └──────────┘      └──────────┘      └──────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                             ▼
                      ┌──────────────┐
                      │    order     │
                      │    :8083     │
                      │  /dashboard  │
                      └──────────────┘
```

---

## 📦 Docker Files

### Dockerfiles
- ✅ `eureka-server/Dockerfile`
- ✅ `product-service/Dockerfile`
- ✅ `inventory-service/Dockerfile`
- ✅ `order-service/Dockerfile`
- ✅ `payment-service/Dockerfile`

### Compose
- ✅ `docker-compose.yml` (root directory)

### Base Image
All services use: **openjdk:17-jdk-slim**

---

## 🔧 Common Commands

```bash
# Start services in background
docker-compose up -d --build

# Rebuild single service
docker-compose up -d --build order-service

# Check running containers
docker-compose ps

# Stop without removing
docker-compose stop

# Clean everything
docker-compose down -v
```

---

## 🐛 Troubleshooting

### Services not starting?
```bash
docker-compose logs <service-name>
```

### Port already in use?
```bash
# Stop conflicting process or change port in docker-compose.yml
lsof -i :8761
```

### Services not visible in Eureka?
- Wait 30-60 seconds for registration
- Check logs: `docker-compose logs eureka-server`
- Verify network: `docker network inspect microservices-network`

---

## 📚 Full Documentation

See **DOCKER_SETUP_GUIDE.md** for:
- Detailed architecture
- Health checks configuration
- Production recommendations
- Security considerations
- Advanced troubleshooting

---

## ⚡ Network

**Name:** `microservices-network`  
**Type:** Bridge  
**Purpose:** Allows service-to-service communication by name

All services can reach each other using:
- `http://product-service:8081`
- `http://inventory-service:8082`
- `http://order-service:8083`
- `http://payment-service:8084`

---

## 🎯 What's Included

✅ Multi-stage Docker builds  
✅ Service discovery (Eureka)  
✅ Health checks  
✅ Auto-restart on failure  
✅ Bridge network for isolation  
✅ Proper startup order  
✅ Exposed ports for UI access  

**Ready for local development! 🚀**
