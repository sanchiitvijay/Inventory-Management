# Kubernetes Deployment Guide

This guide will help you deploy and test the microservices on Kubernetes.

## Prerequisites

1. **Docker Desktop** with Kubernetes enabled
   - Open Docker Desktop → Settings → Kubernetes → Enable Kubernetes
   - Apply & Restart

   OR

2. **Minikube** installed
   ```bash
   # Install Minikube (macOS)
   brew install minikube
   
   # Start Minikube
   minikube start
   ```

3. **kubectl** installed (comes with Docker Desktop)
   ```bash
   kubectl version --client
   ```

4. **jq** for JSON parsing
   ```bash
   brew install jq
   ```

---

## Quick Start

### 1. Deploy to Kubernetes

```bash
# Deploy everything (build images, deploy, test)
./k8s-deploy.sh
```

This script will:
- ✅ Check prerequisites
- 🏗️ Build Docker images for all services
- 🚀 Deploy to Kubernetes cluster
- 🔄 Set up port forwarding
- 🧪 Test all services
- 📊 Populate test data

### 2. Access Services

Once deployed, access services at:
- **Eureka Dashboard**: http://localhost:8761
- **Order Dashboard**: http://localhost:8083/dashboard
- **Product API**: http://localhost:8081/products
- **Inventory API**: http://localhost:8082/inventory
- **Payment API**: http://localhost:8084/payments

### 3. Clean Up

```bash
# Remove all Kubernetes resources
./k8s-cleanup.sh
```

---

## Deployment Options

### Skip Specific Steps

```bash
# Skip building Docker images (if already built)
./k8s-deploy.sh --skip-build

# Skip deployment (just test existing deployment)
./k8s-deploy.sh --skip-deploy

# Skip testing
./k8s-deploy.sh --skip-test
```

---

## Manual Deployment Steps

If you prefer manual control:

### Step 1: Build Docker Images

```bash
cd eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:latest .
cd ..

cd product-service
mvn clean package -DskipTests
docker build -t product-service:latest .
cd ..

# Repeat for inventory-service, order-service, payment-service
```

### Step 2: Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/namespace.yml

# Deploy Eureka Server first
kubectl apply -f k8s/eureka-server/

# Wait for Eureka to be ready
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices-demo --timeout=180s

# Deploy other services
kubectl apply -f k8s/product-service/
kubectl apply -f k8s/inventory-service/
kubectl apply -f k8s/order-service/
kubectl apply -f k8s/payment-service/
```

### Step 3: Set Up Port Forwarding

```bash
# Forward all service ports
kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761 &
kubectl port-forward -n microservices-demo svc/product-service 8081:8081 &
kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082 &
kubectl port-forward -n microservices-demo svc/order-service 8083:8083 &
kubectl port-forward -n microservices-demo svc/payment-service 8084:8084 &
```

### Step 4: Test Services

```bash
# Check health of all services
curl http://localhost:8761/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# Populate test data
./dummyData.sh
```

---

## Useful Kubernetes Commands

### View Resources

```bash
# Get all pods
kubectl get pods -n microservices-demo

# Get all services
kubectl get services -n microservices-demo

# Get all deployments
kubectl get deployments -n microservices-demo

# Watch pod status in real-time
kubectl get pods -n microservices-demo -w
```

### View Logs

```bash
# View logs for a specific service
kubectl logs -f -l app=eureka-server -n microservices-demo
kubectl logs -f -l app=product-service -n microservices-demo

# View logs for a specific pod
kubectl logs -f <pod-name> -n microservices-demo

# View logs for all pods
kubectl logs -f --all-containers=true -n microservices-demo
```

### Describe Resources

```bash
# Get detailed info about a pod
kubectl describe pod <pod-name> -n microservices-demo

# Get detailed info about a service
kubectl describe service <service-name> -n microservices-demo

# Get events
kubectl get events -n microservices-demo --sort-by='.lastTimestamp'
```

### Scale Services

```bash
# Scale product service to 3 replicas
kubectl scale deployment product-service --replicas=3 -n microservices-demo

# Scale all services
kubectl scale deployment --all --replicas=2 -n microservices-demo
```

### Execute Commands in Pods

```bash
# Get a shell in a pod
kubectl exec -it <pod-name> -n microservices-demo -- /bin/sh

# Run a command in a pod
kubectl exec <pod-name> -n microservices-demo -- curl localhost:8081/actuator/health
```

### Update Deployment

```bash
# Update image
kubectl set image deployment/product-service product-service=product-service:v2.0 -n microservices-demo

# Restart a deployment
kubectl rollout restart deployment/product-service -n microservices-demo

# Check rollout status
kubectl rollout status deployment/product-service -n microservices-demo

# Rollback to previous version
kubectl rollout undo deployment/product-service -n microservices-demo
```

---

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n microservices-demo

# Check pod events
kubectl describe pod <pod-name> -n microservices-demo

# Check logs
kubectl logs <pod-name> -n microservices-demo

# Check previous container logs (if pod restarted)
kubectl logs <pod-name> -n microservices-demo --previous
```

### Services Not Accessible

```bash
# Check if port forwarding is running
ps aux | grep "kubectl port-forward"

# Restart port forwarding
pkill -f "kubectl port-forward"
kubectl port-forward -n microservices-demo svc/product-service 8081:8081 &

# Check if service is running
kubectl get svc -n microservices-demo

# Check endpoints
kubectl get endpoints -n microservices-demo
```

### Image Pull Errors

If using local images with Minikube:

```bash
# Use Minikube's Docker daemon
eval $(minikube docker-env)

# Rebuild images
docker build -t product-service:latest ./product-service

# Reset to local Docker daemon
eval $(minikube docker-env -u)
```

### Clean Restart

```bash
# Delete everything and start fresh
./k8s-cleanup.sh
./k8s-deploy.sh
```

---

## Kubernetes Dashboard (Optional)

### Enable Dashboard

```bash
# For Minikube
minikube dashboard

# For Docker Desktop, install dashboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Create admin user
kubectl create serviceaccount dashboard-admin-sa -n kubernetes-dashboard
kubectl create clusterrolebinding dashboard-admin-sa --clusterrole=cluster-admin --serviceaccount=kubernetes-dashboard:dashboard-admin-sa

# Get access token
kubectl create token dashboard-admin-sa -n kubernetes-dashboard

# Access dashboard
kubectl proxy
# Open: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

---

## Architecture in Kubernetes

```
┌─────────────────────────────────────┐
│     Kubernetes Cluster              │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Namespace: microservices-   │  │
│  │           demo                │  │
│  │                               │  │
│  │  ┌────────────────────┐      │  │
│  │  │  Eureka Server     │      │  │
│  │  │  (1 replica)       │      │  │
│  │  │  Port: 8761        │      │  │
│  │  └────────────────────┘      │  │
│  │           ↓                   │  │
│  │  ┌────────────────────┐      │  │
│  │  │  Product Service   │      │  │
│  │  │  (3 replicas)      │      │  │
│  │  │  Port: 8081        │      │  │
│  │  └────────────────────┘      │  │
│  │                               │  │
│  │  ┌────────────────────┐      │  │
│  │  │  Inventory Service │      │  │
│  │  │  (3 replicas)      │      │  │
│  │  │  Port: 8082        │      │  │
│  │  └────────────────────┘      │  │
│  │                               │  │
│  │  ┌────────────────────┐      │  │
│  │  │  Order Service     │      │  │
│  │  │  (3 replicas)      │      │  │
│  │  │  Port: 8083        │      │  │
│  │  └────────────────────┘      │  │
│  │                               │  │
│  │  ┌────────────────────┐      │  │
│  │  │  Payment Service   │      │  │
│  │  │  (3 replicas)      │      │  │
│  │  │  Port: 8084        │      │  │
│  │  └────────────────────┘      │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
          ↕
   Port Forwarding
          ↕
      localhost:876X
```

---

## Performance Testing in Kubernetes

### Load Testing with curl

```bash
# Test product service under load
for i in {1..100}; do
  curl -s http://localhost:8081/products > /dev/null &
done
wait

# Monitor pod resource usage
kubectl top pods -n microservices-demo
```

### Stress Testing

```bash
# Install hey (HTTP load generator)
brew install hey

# Load test product service
hey -n 1000 -c 10 http://localhost:8081/products

# Load test with POST requests
hey -n 100 -c 5 -m POST \
  -H "Content-Type: application/json" \
  -d '{"productSku":"TEST-001","available":100,"threshold":10}' \
  http://localhost:8082/inventory
```

---

## Production Considerations

### Resource Limits

Add resource limits to deployments:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### Horizontal Pod Autoscaling

```bash
# Enable autoscaling
kubectl autoscale deployment product-service \
  --cpu-percent=50 \
  --min=2 \
  --max=10 \
  -n microservices-demo
```

### Persistent Storage

For production, replace H2 with persistent databases:
- PostgreSQL or MySQL for relational data
- Add PersistentVolumeClaims (PVC)
- Use StatefulSets for databases

### Ingress Controller

Instead of port-forwarding, use an Ingress:

```bash
# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# Create Ingress resource
kubectl apply -f k8s/ingress.yml
```

---

## Summary

- ✅ Automated deployment with `k8s-deploy.sh`
- ✅ Easy cleanup with `k8s-cleanup.sh`
- ✅ Port forwarding for local access
- ✅ Health checks and monitoring
- ✅ Test data population
- ✅ Scalable architecture
- ✅ Production-ready configurations

For more information, see the main [README.md](../README.md).
