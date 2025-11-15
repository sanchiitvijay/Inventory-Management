# Kubernetes Setup & Deployment - Quick Start Guide

## 📋 Prerequisites Setup

### Step 1: Enable Kubernetes in Docker Desktop

Since you have Docker Desktop installed, enable Kubernetes:

1. **Open Docker Desktop**
2. Click on the **Settings/Preferences** icon (gear icon)
3. Navigate to **Kubernetes** in the left sidebar
4. Check the box **"Enable Kubernetes"**
5. Click **"Apply & Restart"**
6. Wait 2-3 minutes for Kubernetes to start

### Step 2: Verify Installation

```bash
# Check if Kubernetes is running
kubectl cluster-info

# Check kubectl version
kubectl version --client

# You should see output showing Kubernetes is running on localhost
```

---

## 🚀 Quick Deployment (Automated)

Once Kubernetes is enabled, deploy everything with one command:

```bash
# Navigate to project directory
cd /Users/sanchitvijay/working/temp2

# Run the deployment script
./k8s-deploy.sh
```

**What this script does:**
1. ✅ Checks all prerequisites (kubectl, Docker, K8s cluster)
2. 🏗️ Builds Docker images for all 5 services
3. 🚀 Deploys services to Kubernetes
4. ⏳ Waits for pods to be ready
5. 🔄 Sets up port forwarding
6. 🧪 Tests all service health endpoints
7. 📊 Populates test data using dummyData.sh

---

## 📊 What Gets Deployed

### Namespace
- `microservices-demo` - Isolated namespace for all services

### Services Deployed
1. **Eureka Server** (1 replica) - Service Discovery
2. **Product Service** (3 replicas) - Product Management
3. **Inventory Service** (3 replicas) - Inventory Management
4. **Order Service** (3 replicas) - Order Management
5. **Payment Service** (3 replicas) - Payment Processing

### Port Forwarding
- Eureka Server: `localhost:8761`
- Product Service: `localhost:8081`
- Inventory Service: `localhost:8082`
- Order Service: `localhost:8083`
- Payment Service: `localhost:8084`

---

## 🧪 Testing the Deployment

### Check Pod Status
```bash
kubectl get pods -n microservices-demo
```

Expected output:
```
NAME                                  READY   STATUS    RESTARTS   AGE
eureka-server-xxxxxxxxx-xxxxx         1/1     Running   0          2m
product-service-xxxxxxxxx-xxxxx       1/1     Running   0          2m
product-service-xxxxxxxxx-xxxxx       1/1     Running   0          2m
product-service-xxxxxxxxx-xxxxx       1/1     Running   0          2m
inventory-service-xxxxxxxxx-xxxxx     1/1     Running   0          2m
...
```

### Test Service Health
```bash
# Test all services
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8081/actuator/health  # Product
curl http://localhost:8082/actuator/health  # Inventory
curl http://localhost:8083/actuator/health  # Order
curl http://localhost:8084/actuator/health  # Payment
```

### Access Dashboards
- **Eureka Dashboard**: http://localhost:8761
- **Order Dashboard**: http://localhost:8083/dashboard

### Run Test Data Script
```bash
./dummyData.sh
```

---

## 📝 Manual Deployment (Step by Step)

If you prefer manual control:

### 1. Build Docker Images
```bash
cd /Users/sanchitvijay/working/temp2

# Build each service
for service in eureka-server product-service inventory-service order-service payment-service; do
    echo "Building $service..."
    cd $service
    mvn clean package -DskipTests
    docker build -t ${service}:latest .
    cd ..
done
```

### 2. Deploy to Kubernetes
```bash
# Create namespace
kubectl apply -f k8s/namespace.yml

# Deploy Eureka Server first (others depend on it)
kubectl apply -f k8s/eureka-server/

# Wait for Eureka to be ready
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices-demo --timeout=180s

# Deploy other services
kubectl apply -f k8s/product-service/
kubectl apply -f k8s/inventory-service/
kubectl apply -f k8s/order-service/
kubectl apply -f k8s/payment-service/
```

### 3. Set Up Port Forwarding
```bash
# Forward all service ports (run in background)
kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761 &
kubectl port-forward -n microservices-demo svc/product-service 8081:8081 &
kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082 &
kubectl port-forward -n microservices-demo svc/order-service 8083:8083 &
kubectl port-forward -n microservices-demo svc/payment-service 8084:8084 &

# Wait for port forwards to establish
sleep 5
```

### 4. Test Services
```bash
# Check all health endpoints
for port in 8761 8081 8082 8083 8084; do
    echo "Testing localhost:$port..."
    curl -s http://localhost:$port/actuator/health | jq .status
done

# Populate test data
./dummyData.sh
```

---

## 🔍 Monitoring & Debugging

### View Logs
```bash
# View logs for a specific service
kubectl logs -f -l app=product-service -n microservices-demo

# View logs from all pods
kubectl logs -f --all-containers=true -n microservices-demo

# View logs from a specific pod
kubectl logs -f <pod-name> -n microservices-demo
```

### Check Service Status
```bash
# Get all resources
kubectl get all -n microservices-demo

# Get detailed pod information
kubectl describe pod <pod-name> -n microservices-demo

# Watch pods in real-time
kubectl get pods -n microservices-demo -w
```

### Scale Services
```bash
# Scale product service to 5 replicas
kubectl scale deployment product-service --replicas=5 -n microservices-demo

# Check scaling
kubectl get pods -n microservices-demo -l app=product-service
```

---

## 🧹 Cleanup

### Stop Port Forwarding
```bash
# Kill all port-forward processes
pkill -f "kubectl port-forward"
```

### Delete All Resources
```bash
# Use the cleanup script
./k8s-cleanup.sh

# Or manually
kubectl delete namespace microservices-demo
```

### Remove Docker Images (Optional)
```bash
docker rmi eureka-server:latest product-service:latest inventory-service:latest order-service:latest payment-service:latest
```

---

## 🐛 Troubleshooting

### Issue: Pods Not Starting
```bash
# Check pod status
kubectl get pods -n microservices-demo

# Check pod events
kubectl describe pod <pod-name> -n microservices-demo

# Common issues:
# - ImagePullBackOff: Image not found (rebuild images)
# - CrashLoopBackOff: Application error (check logs)
# - Pending: Resource constraints (check cluster resources)
```

### Issue: Services Not Accessible
```bash
# Check if port forwarding is running
ps aux | grep "kubectl port-forward"

# Restart port forwarding
pkill -f "kubectl port-forward"
./k8s-deploy.sh --skip-build --skip-deploy
```

### Issue: Kubernetes Not Starting
```bash
# Reset Kubernetes in Docker Desktop
# Settings → Kubernetes → Reset Kubernetes Cluster

# Or check Docker Desktop is running
docker info
```

---

## 📚 Next Steps

1. **Enable Kubernetes** in Docker Desktop (if not already done)
2. **Run deployment script**: `./k8s-deploy.sh`
3. **Access dashboards**:
   - Eureka: http://localhost:8761
   - Order Dashboard: http://localhost:8083/dashboard
4. **Test APIs** using the dummy data script
5. **Monitor** using kubectl commands
6. **Clean up** when done: `./k8s-cleanup.sh`

---

## 📖 Additional Resources

- Full Kubernetes Guide: [k8s/K8S_DEPLOYMENT_GUIDE.md](K8S_DEPLOYMENT_GUIDE.md)
- Main README: [README.md](../README.md)
- Testing Guide: [TESTING_GUIDE.md](../TESTING_GUIDE.md)

---

## ✅ Summary

You now have:
- ✅ Automated deployment script (`k8s-deploy.sh`)
- ✅ Cleanup script (`k8s-cleanup.sh`)
- ✅ Complete Kubernetes manifests in `k8s/` directory
- ✅ Port forwarding setup
- ✅ Test data population
- ✅ Health monitoring

**Ready to deploy? Run: `./k8s-deploy.sh`**
