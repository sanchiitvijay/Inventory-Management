#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${GREEN}===================================${NC}"
echo -e "${GREEN}Kubernetes Minikube Deployment${NC}"
echo -e "${GREEN}===================================${NC}"

# Check if Minikube is running
if ! minikube status | grep -q "Running"; then
    echo -e "${RED}Error: Minikube is not running${NC}"
    echo -e "${YELLOW}Start Minikube with: minikube start${NC}"
    exit 1
fi

# Configure Docker to use Minikube's daemon
echo -e "\n${YELLOW}Configuring Docker for Minikube...${NC}"
eval $(minikube -p minikube docker-env)

# Build Docker images
SERVICES=("eureka-server" "product-service" "inventory-service" "order-service" "payment-service")

echo -e "\n${CYAN}Building Docker images inside Minikube...${NC}"
for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}Building $service...${NC}"
    cd /Users/sanchitvijay/working/temp2/$service
    
    # Check if JAR exists
    if [ ! -f "target/${service}-1.0.0-SNAPSHOT.jar" ]; then
        echo -e "${RED}Error: JAR file not found for $service${NC}"
        echo -e "${YELLOW}Please run: mvn clean package -DskipTests${NC}"
        exit 1
    fi
    
    # Build Docker image
    docker build -t $service:latest . 2>&1 | tail -3
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        echo -e "${GREEN}✓ Successfully built $service:latest${NC}"
    else
        echo -e "${RED}✗ Failed to build $service${NC}"
        exit 1
    fi
done

# Create namespace
echo -e "\n${YELLOW}Creating namespace...${NC}"
kubectl create namespace microservices-demo --dry-run=client -o yaml | kubectl apply -f -

# Deploy to Kubernetes
echo -e "\n${YELLOW}Deploying services to Kubernetes...${NC}"

# Deploy Eureka Server first
echo -e "${YELLOW}Deploying Eureka Server...${NC}"
kubectl apply -f /Users/sanchitvijay/working/temp2/k8s/eureka-server/ -n microservices-demo

# Wait for Eureka to be ready
echo -e "${YELLOW}Waiting for Eureka Server to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices-demo --timeout=120s

# Deploy other services
for service in product-service inventory-service order-service payment-service; do
    echo -e "${YELLOW}Deploying $service...${NC}"
    kubectl apply -f /Users/sanchitvijay/working/temp2/k8s/$service/ -n microservices-demo
done

# Wait for all pods to be ready
echo -e "\n${YELLOW}Waiting for all pods to be ready...${NC}"
kubectl wait --for=condition=ready pod --all -n microservices-demo --timeout=300s || {
    echo -e "${RED}Some pods failed to start. Checking status...${NC}"
    kubectl get pods -n microservices-demo
    echo -e "\n${YELLOW}Pod descriptions:${NC}"
    kubectl describe pods -n microservices-demo | grep -A 5 "Events:"
}

# Display status
echo -e "\n${GREEN}===================================${NC}"
echo -e "${GREEN}Deployment Status${NC}"
echo -e "${GREEN}===================================${NC}"
kubectl get pods -n microservices-demo -o wide
echo ""
kubectl get services -n microservices-demo

echo -e "\n${GREEN}===================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}===================================${NC}"

# Set up port forwarding automatically
echo -e "\n${CYAN}Setting up port forwarding...${NC}"

# Kill any existing port forwards
pkill -f "kubectl port-forward" 2>/dev/null || true
sleep 2

# Start port forwarding in background
kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/product-service 8081:8081 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/order-service 8083:8083 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/payment-service 8084:8084 > /dev/null 2>&1 &

sleep 3

echo -e "${GREEN}✓ Port forwarding active${NC}"
echo -e "\n${CYAN}Access services at:${NC}"
echo -e "  Eureka Server:     ${YELLOW}http://localhost:8761${NC}"
echo -e "  Product Service:   ${YELLOW}http://localhost:8081/products${NC}"
echo -e "  Inventory Service: ${YELLOW}http://localhost:8082/inventory${NC}"
echo -e "  Order Service:     ${YELLOW}http://localhost:8083/dashboard${NC}"
echo -e "  Payment Service:   ${YELLOW}http://localhost:8084/payments${NC}"

echo -e "\n${CYAN}To stop port forwarding:${NC}"
echo -e "  ${YELLOW}pkill -f 'kubectl port-forward'${NC}"

echo -e "\n${CYAN}To view logs:${NC}"
echo -e "  ${YELLOW}kubectl logs -f -l app=eureka-server -n microservices-demo${NC}"
echo -e "  ${YELLOW}kubectl logs -f -l app=product-service -n microservices-demo${NC}"

echo -e "\n${CYAN}To cleanup:${NC}"
echo -e "  ${YELLOW}kubectl delete namespace microservices-demo${NC}"
echo -e "  ${YELLOW}pkill -f 'kubectl port-forward'${NC}"

echo -e "\n${GREEN}Ready to test! Run ./dummyData.sh to populate test data.${NC}"
