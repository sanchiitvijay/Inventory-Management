# Health Endpoints and Actuator Configuration

## Overview

All microservices now include Spring Boot Actuator for health monitoring, metrics, and operational insights. The `/actuator/health` endpoint is configured with readiness and liveness probes for Kubernetes.

## Changes Made

### 1. Added Dependencies

Added `spring-boot-starter-actuator` to all service `pom.xml` files:
- ✅ eureka-server
- ✅ product-service
- ✅ inventory-service
- ✅ order-service
- ✅ payment-service

### 2. Updated Application Configuration

Added management endpoints configuration to all `application.yml` files:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

**Configuration Details:**
- `include: "*"` - Exposes all actuator endpoints (for development)
- `show-details: always` - Shows detailed health information

### 3. Kubernetes Health Probes

All Kubernetes deployments now include:

#### Readiness Probe
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: <service-port>
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

#### Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: <service-port>
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

## Available Endpoints

### Core Health Endpoints

| Endpoint | Purpose | Example |
|----------|---------|---------|
| `/actuator/health` | Overall application health | `http://localhost:8081/actuator/health` |
| `/actuator/health/liveness` | Liveness state (for K8s) | `http://localhost:8081/actuator/health/liveness` |
| `/actuator/health/readiness` | Readiness state (for K8s) | `http://localhost:8081/actuator/health/readiness` |

### Additional Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator` | List of available endpoints |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/metrics/{metric}` | Specific metric details |
| `/actuator/env` | Environment properties |
| `/actuator/configprops` | Configuration properties |
| `/actuator/beans` | Spring beans |
| `/actuator/mappings` | Request mappings |
| `/actuator/loggers` | Logger configuration |
| `/actuator/threaddump` | Thread dump |
| `/actuator/heapdump` | Heap dump (download) |

## Service Ports

| Service | Port | Health URL |
|---------|------|------------|
| Eureka Server | 8761 | `http://localhost:8761/actuator/health` |
| Product Service | 8081 | `http://localhost:8081/actuator/health` |
| Inventory Service | 8082 | `http://localhost:8082/actuator/health` |
| Order Service | 8083 | `http://localhost:8083/actuator/health` |
| Payment Service | 8084 | `http://localhost:8084/actuator/health` |

## Testing Health Endpoints

### Local Testing (Docker Compose)

Start services and test health endpoints:

```bash
# Start all services
docker-compose up -d

# Wait for services to start
sleep 30

# Test Eureka Server health
curl http://localhost:8761/actuator/health

# Test Product Service health
curl http://localhost:8081/actuator/health

# Test Inventory Service health
curl http://localhost:8082/actuator/health

# Test Order Service health
curl http://localhost:8083/actuator/health

# Test Payment Service health
curl http://localhost:8084/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 100068036608,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Kubernetes Testing

After deploying to Kubernetes:

```bash
# Deploy services
kubectl apply -f k8s/

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app=product-service -n microservices-demo --timeout=120s

# Port forward to access health endpoint
kubectl port-forward -n microservices-demo svc/product-service 8081:8081

# In another terminal, test the endpoint
curl http://localhost:8081/actuator/health
```

### Check Probe Status in Kubernetes

```bash
# Check pod status
kubectl get pods -n microservices-demo

# Describe pod to see probe results
kubectl describe pod <pod-name> -n microservices-demo

# Check pod events for probe failures
kubectl get events -n microservices-demo --sort-by='.lastTimestamp'

# Check logs if health checks are failing
kubectl logs -n microservices-demo <pod-name>
```

## Health Response Examples

### Healthy Service
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "eureka": {
      "status": "UP",
      "details": {
        "applications": {}
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Service with Issues
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Readiness vs Liveness

### Readiness Probe (`/actuator/health/readiness`)
- Determines if the pod is ready to accept traffic
- If fails: Pod removed from service load balancer
- Pod is NOT restarted
- Use for: Dependencies not ready (DB connections, external services)

### Liveness Probe (`/actuator/health/liveness`)
- Determines if the pod is alive and functioning
- If fails: Pod is restarted by Kubernetes
- Use for: Deadlocks, hung processes, unrecoverable errors

## Monitoring Best Practices

### 1. Production Configuration

For production, restrict exposed endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 2. Security Considerations

Add Spring Security to protect actuator endpoints:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Configure security:
```yaml
management:
  endpoints:
    web:
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}
```

### 3. Integrate with Monitoring Tools

- **Prometheus**: Use `/actuator/prometheus` endpoint
- **Grafana**: Create dashboards from Prometheus metrics
- **ELK Stack**: Send logs with correlation IDs
- **Kubernetes Dashboard**: View health status

### 4. Custom Health Indicators

Create custom health checks:

```java
@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        boolean isHealthy = checkExternalService();
        if (isHealthy) {
            return Health.up()
                .withDetail("service", "external-api")
                .build();
        }
        return Health.down()
            .withDetail("service", "external-api")
            .withDetail("error", "Connection timeout")
            .build();
    }
}
```

## Troubleshooting

### Health Check Failures

1. **Check logs**:
   ```bash
   kubectl logs -n microservices-demo <pod-name>
   ```

2. **Verify endpoint accessibility**:
   ```bash
   kubectl exec -it -n microservices-demo <pod-name> -- curl http://localhost:8081/actuator/health
   ```

3. **Check probe configuration**:
   ```bash
   kubectl get pod <pod-name> -n microservices-demo -o yaml | grep -A 10 "livenessProbe\|readinessProbe"
   ```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Pod not ready | Readiness probe failing | Check dependencies (DB, Eureka) |
| Pod restarting | Liveness probe failing | Increase `initialDelaySeconds` |
| 404 on health endpoint | Actuator not configured | Verify dependency and config |
| Slow startup | Not enough resources | Increase CPU/memory limits |

## Next Steps

1. **Add Prometheus metrics**: Add `micrometer-registry-prometheus` dependency
2. **Implement custom health indicators**: For external service dependencies
3. **Set up monitoring**: Deploy Prometheus + Grafana
4. **Configure alerts**: Alert on health check failures
5. **Add distributed tracing**: Integrate Spring Cloud Sleuth + Zipkin

## Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Kubernetes Liveness and Readiness Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Spring Boot Health Indicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
