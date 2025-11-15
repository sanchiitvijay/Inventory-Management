#!/bin/bash

# Kubernetes Deployment Script for Minikube
# This script configures Minikube and deploys all microservices

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Minikube Deployment Tool${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Check if Minikube is running
if ! minikube status &> /dev/null; then
    echo -e "${YELLOW}Starting Minikube...${NC}"
    minikube start
    echo ""
fi

echo -e "${GREEN}✓${NC} Minikube is running"
echo ""

# Configure Docker to use Minikube's daemon
echo -e "${YELLOW}Configuring Docker to use Minikube's daemon...${NC}"
eval $(minikube docker-env)
echo -e "${GREEN}✓${NC} Docker configured for Minikube"
echo ""

# Build images
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Building Docker Images${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

SERVICES=("eureka-server" "product-service" "inventory-service" "order-service" "payment-service")

for service in "${SERVICES[@]}"; do
    echo -e "${MAGENTA}Building ${service}...${NC}"
    cd "$service"
    
    # Build JAR
    if [ ! -f "target/${service}-1.0.0-SNAPSHOT.jar" ]; then
        echo "  Building JAR..."
        mvn clean package -DskipTests -q
    fi
    
    # Build Docker image
    docker build -t "${service}:latest" . > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✓${NC} Built ${service}:latest"
    else
        echo -e "  ${RED}✗${NC} Failed to build ${service}"
        exit 1
    fi
    
    cd ..
done

echo ""
echo -e "${GREEN}✓ All images built in Minikube${NC}"
echo ""

# Deploy to Kubernetes
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Deploying to Kubernetes${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Create namespace
kubectl apply -f k8s/namespace.yml

# Deploy Eureka first
echo -e "${MAGENTA}Deploying Eureka Server...${NC}"
kubectl apply -f k8s/eureka-server/
echo ""

# Wait for Eureka
echo -e "${YELLOW}Waiting for Eureka Server to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices-demo --timeout=180s || true
sleep 10
echo ""

# Deploy other services
for service in "${SERVICES[@]:1}"; do
    echo -e "${MAGENTA}Deploying ${service}...${NC}"
    kubectl apply -f "k8s/${service}/"
    sleep 3
done

echo ""
echo -e "${GREEN}✓ All services deployed${NC}"
echo ""

# Check status
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Deployment Status${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

echo -e "${CYAN}Pods:${NC}"
kubectl get pods -n microservices-demo
echo ""

echo -e "${CYAN}Services:${NC}"
kubectl get services -n microservices-demo
echo ""

# Wait for all pods
echo -e "${YELLOW}Waiting for all pods to be ready...${NC}"
kubectl wait --for=condition=ready pod --all -n microservices-demo --timeout=180s || true
echo ""

echo -e "${CYAN}Final Status:${NC}"
kubectl get pods -n microservices-demo
echo ""

# Port forwarding setup
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Setting up Access${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Kill existing port forwards
pkill -f "kubectl port-forward" 2>/dev/null || true
sleep 2

echo -e "${YELLOW}Setting up port forwarding...${NC}"
kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/product-service 8081:8081 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/order-service 8083:8083 > /dev/null 2>&1 &
kubectl port-forward -n microservices-demo svc/payment-service 8084:8084 > /dev/null 2>&1 &

sleep 5

echo ""
echo -e "${GREEN}✓ Port forwarding active${NC}"
echo ""

# Test services
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Testing Services${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

sleep 5

SERVICES_TO_TEST=("8761:Eureka" "8081:Product" "8082:Inventory" "8083:Order" "8084:Payment")

for service_info in "${SERVICES_TO_TEST[@]}"; do
    port="${service_info%%:*}"
    name="${service_info##*:}"
    
    status=$(curl -s http://localhost:$port/actuator/health 2>/dev/null | jq -r '.status' || echo "DOWN")
    
    if [ "$status" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} $name Service (port $port): UP"
    else
        echo -e "  ${YELLOW}⚠${NC} $name Service (port $port): $status (may still be starting)"
    fi
done

echo ""

# Populate test data
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Populating Test Data${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [ -f "./dummyData.sh" ]; then
    echo -e "${YELLOW}Running dummyData.sh...${NC}"
    sleep 10  # Give services more time to stabilize
    ./dummyData.sh
else
    echo -e "${YELLOW}dummyData.sh not found${NC}"
fi

echo ""

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✓ Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${CYAN}Access Points:${NC}"
echo -e "  • Eureka Dashboard:  ${BLUE}http://localhost:8761${NC}"
echo -e "  • Order Dashboard:   ${BLUE}http://localhost:8083/dashboard${NC}"
echo -e "  • Product API:       ${BLUE}http://localhost:8081/products${NC}"
echo -e "  • Inventory API:     ${BLUE}http://localhost:8082/inventory${NC}"
echo -e "  • Payment API:       ${BLUE}http://localhost:8084/payments${NC}"
echo ""
echo -e "${CYAN}Useful Commands:${NC}"
echo -e "  • View pods:         ${BLUE}kubectl get pods -n microservices-demo${NC}"
echo -e "  • View logs:         ${BLUE}kubectl logs -f -l app=product-service -n microservices-demo${NC}"
echo -e "  • Stop port forward: ${BLUE}pkill -f 'kubectl port-forward'${NC}"
echo -e "  • Clean up:          ${BLUE}./k8s-cleanup.sh${NC}"
echo ""
echo -e "${GREEN}Services are running in Minikube! 🚀${NC}"
