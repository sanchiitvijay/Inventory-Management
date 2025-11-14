# Kubernetes Deployment Guide

## Prerequisites
- Kubernetes cluster (Minikube, Docker Desktop, or cloud provider)
- kubectl CLI installed and configured
- Docker installed

## Step 1: Build Docker Images

Build all service images from the project root:

```bash
# Build Eureka Server
docker build -t eureka-server:latest ./eureka-server

# Build Product Service
docker build -t product-service:latest ./product-service

# Build Inventory Service
docker build -t inventory-service:latest ./inventory-service

# Build Order Service
docker build -t order-service:latest ./order-service

# Build Payment Service
docker build -t payment-service:latest ./payment-service
```

### For Minikube Users

If using Minikube, load images into Minikube's Docker daemon:

```bash
# Option 1: Build inside Minikube
eval $(minikube docker-env)
# Then run the docker build commands above

# Option 2: Load existing images
minikube image load eureka-server:latest
minikube image load product-service:latest
minikube image load inventory-service:latest
minikube image load order-service:latest
minikube image load payment-service:latest
```

## Step 2: Deploy to Kubernetes

Deploy all manifests from the project root:

```bash
kubectl apply -f k8s/
```

This will create:
- Namespace: `microservices-demo`
- Eureka Server (1 replica)
- Product Service (3 replicas)
- Inventory Service (3 replicas)
- Order Service (3 replicas)
- Payment Service (3 replicas)

## Step 3: Verify Deployment

Check deployment status:

```bash
# Check all resources
kubectl get all -n microservices-demo

# Check pods
kubectl get pods -n microservices-demo

# Check services
kubectl get svc -n microservices-demo

# Check configmaps
kubectl get configmaps -n microservices-demo
```

## Step 4: Access Services

### Access Eureka Dashboard

```bash
kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761
```

Then visit: http://localhost:8761

### Access Individual Services

```bash
# Product Service
kubectl port-forward -n microservices-demo svc/product-service 8081:8081

# Inventory Service
kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082

# Order Service
kubectl port-forward -n microservices-demo svc/order-service 8083:8083

# Payment Service
kubectl port-forward -n microservices-demo svc/payment-service 8084:8084
```

## Service Configuration

### Eureka Server
- Service Port: 8761
- Type: ClusterIP
- Replicas: 1

### Microservices
- Product Service: Port 8081 (3 replicas)
- Inventory Service: Port 8082 (3 replicas)
- Order Service: Port 8083 (3 replicas)
- Payment Service: Port 8084 (3 replicas)

All microservices register with Eureka at: `http://eureka-server:8761/eureka/`

## Example: Order Service with Environment Variables

The `order-service` deployment includes environment variable configuration for the Eureka URL:

```yaml
env:
- name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
  value: "http://eureka-server:8761/eureka/"
- name: SPRING_APPLICATION_NAME
  value: "order-service"
```

These environment variables override the ConfigMap values and can be used for dynamic configuration.

## Useful Commands

```bash
# View logs for a specific service
kubectl logs -f -n microservices-demo deployment/order-service

# Describe a deployment
kubectl describe deployment order-service -n microservices-demo

# Scale a deployment
kubectl scale deployment order-service --replicas=5 -n microservices-demo

# Delete all resources
kubectl delete namespace microservices-demo

# Restart a deployment
kubectl rollout restart deployment/order-service -n microservices-demo
```

## Troubleshooting

### Pods not starting
```bash
kubectl describe pod <pod-name> -n microservices-demo
kubectl logs <pod-name> -n microservices-demo
```

### Image pull errors
Ensure images are built and available. For Minikube, verify images are loaded:
```bash
minikube image ls | grep -E "eureka|product|inventory|order|payment"
```

### Service discovery issues
Check if Eureka Server is running and accessible:
```bash
kubectl get pods -n microservices-demo -l app=eureka-server
kubectl logs -n microservices-demo -l app=eureka-server
```

## Clean Up

To remove all resources:

```bash
kubectl delete -f k8s/
# or
kubectl delete namespace microservices-demo
```
