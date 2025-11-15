#!/bin/bash

# Kubernetes Deployment and Testing Script
# This script builds Docker images, deploys to Kubernetes, and tests the services

set -e  # Exit on error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Kubernetes Deployment & Testing Tool${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}Error: kubectl is not installed${NC}"
        echo "Please install kubectl: https://kubernetes.io/docs/tasks/tools/"
        exit 1
    fi
    echo -e "  ${GREEN}✓${NC} kubectl is installed"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Error: Docker is not installed${NC}"
        exit 1
    fi
    echo -e "  ${GREEN}✓${NC} Docker is installed"
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        echo -e "${RED}Error: Docker is not running${NC}"
        echo "Please start Docker Desktop"
        exit 1
    fi
    echo -e "  ${GREEN}✓${NC} Docker is running"
    
    # Check Kubernetes cluster
    if ! kubectl cluster-info &> /dev/null; then
        echo -e "${YELLOW}⚠${NC}  No Kubernetes cluster detected"
        echo ""
        echo "Please enable Kubernetes in Docker Desktop:"
        echo "  1. Open Docker Desktop"
        echo "  2. Go to Settings/Preferences"
        echo "  3. Click on 'Kubernetes'"
        echo "  4. Check 'Enable Kubernetes'"
        echo "  5. Click 'Apply & Restart'"
        echo ""
        echo "Or install Minikube: https://minikube.sigs.k8s.io/docs/start/"
        echo ""
        read -p "Press Enter to continue if you have a cluster, or Ctrl+C to exit..."
    else
        echo -e "  ${GREEN}✓${NC} Kubernetes cluster is accessible"
        CURRENT_CONTEXT=$(kubectl config current-context)
        echo -e "  ${CYAN}Current context: ${CURRENT_CONTEXT}${NC}"
    fi
    
    echo ""
}

# Function to build Docker images
build_images() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}1. Building Docker Images${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    SERVICES=("eureka-server" "product-service" "inventory-service" "order-service" "payment-service")
    
    for service in "${SERVICES[@]}"; do
        echo -e "${MAGENTA}Building ${service}...${NC}"
        cd "$service"
        
        # Build JAR if not exists
        if [ ! -f "target/${service}-1.0.0-SNAPSHOT.jar" ]; then
            echo "  Building JAR file..."
            mvn clean package -DskipTests -q
        fi
        
        # Build Docker image
        docker build -t "${service}:latest" -t "${service}:v1.0" . > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "  ${GREEN}✓${NC} Built ${service}:latest"
        else
            echo -e "  ${RED}✗${NC} Failed to build ${service}"
            exit 1
        fi
        
        cd ..
    done
    
    echo -e "${GREEN}✓ All Docker images built successfully${NC}"
    echo ""
}

# Function to deploy to Kubernetes
deploy_to_k8s() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}2. Deploying to Kubernetes${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    # Create namespace
    echo -e "${MAGENTA}Creating namespace...${NC}"
    kubectl apply -f k8s/namespace.yml
    echo ""
    
    # Deploy Eureka Server first
    echo -e "${MAGENTA}Deploying Eureka Server...${NC}"
    kubectl apply -f k8s/eureka-server/
    echo ""
    
    # Wait for Eureka to be ready
    echo -e "${YELLOW}Waiting for Eureka Server to be ready (this may take 60-90 seconds)...${NC}"
    kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices-demo --timeout=180s || true
    sleep 10
    echo ""
    
    # Deploy other services
    SERVICES=("product-service" "inventory-service" "order-service" "payment-service")
    
    for service in "${SERVICES[@]}"; do
        echo -e "${MAGENTA}Deploying ${service}...${NC}"
        kubectl apply -f "k8s/${service}/"
        sleep 5
    done
    
    echo ""
    echo -e "${GREEN}✓ All services deployed to Kubernetes${NC}"
    echo ""
}

# Function to check deployment status
check_status() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}3. Checking Deployment Status${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    echo -e "${CYAN}Pods Status:${NC}"
    kubectl get pods -n microservices-demo
    echo ""
    
    echo -e "${CYAN}Services:${NC}"
    kubectl get services -n microservices-demo
    echo ""
    
    echo -e "${YELLOW}Waiting for all pods to be ready (timeout: 3 minutes)...${NC}"
    kubectl wait --for=condition=ready pod --all -n microservices-demo --timeout=180s || true
    echo ""
    
    echo -e "${CYAN}Final Pod Status:${NC}"
    kubectl get pods -n microservices-demo
    echo ""
}

# Function to port-forward services
setup_port_forwarding() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}4. Setting up Port Forwarding${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    echo -e "${YELLOW}Setting up port forwarding for all services...${NC}"
    echo -e "${CYAN}You can access services at:${NC}"
    echo ""
    
    # Kill any existing port-forward processes
    pkill -f "kubectl port-forward" 2>/dev/null || true
    sleep 2
    
    # Port forward Eureka Server
    echo -e "  Eureka Server:       http://localhost:8761"
    kubectl port-forward -n microservices-demo svc/eureka-server 8761:8761 > /dev/null 2>&1 &
    
    # Port forward Product Service
    echo -e "  Product Service:     http://localhost:8081"
    kubectl port-forward -n microservices-demo svc/product-service 8081:8081 > /dev/null 2>&1 &
    
    # Port forward Inventory Service
    echo -e "  Inventory Service:   http://localhost:8082"
    kubectl port-forward -n microservices-demo svc/inventory-service 8082:8082 > /dev/null 2>&1 &
    
    # Port forward Order Service
    echo -e "  Order Service:       http://localhost:8083"
    kubectl port-forward -n microservices-demo svc/order-service 8083:8083 > /dev/null 2>&1 &
    
    # Port forward Payment Service
    echo -e "  Payment Service:     http://localhost:8084"
    kubectl port-forward -n microservices-demo svc/payment-service 8084:8084 > /dev/null 2>&1 &
    
    echo ""
    echo -e "${GREEN}✓ Port forwarding setup complete${NC}"
    echo -e "${YELLOW}Note: Port forwarding will run in the background${NC}"
    echo -e "${YELLOW}To stop: pkill -f 'kubectl port-forward'${NC}"
    echo ""
    
    # Wait for port forwards to be established
    sleep 5
}

# Function to test services
test_services() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}5. Testing Services${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    echo -e "${YELLOW}Waiting for services to be fully ready...${NC}"
    sleep 10
    echo ""
    
    # Test Eureka Server
    echo -e "${CYAN}Testing Eureka Server...${NC}"
    EUREKA_STATUS=$(curl -s http://localhost:8761/actuator/health 2>/dev/null | jq -r '.status' || echo "UNAVAILABLE")
    if [ "$EUREKA_STATUS" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} Eureka Server is UP"
    else
        echo -e "  ${RED}✗${NC} Eureka Server is DOWN"
    fi
    
    # Test Product Service
    echo -e "${CYAN}Testing Product Service...${NC}"
    PRODUCT_STATUS=$(curl -s http://localhost:8081/actuator/health 2>/dev/null | jq -r '.status' || echo "UNAVAILABLE")
    if [ "$PRODUCT_STATUS" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} Product Service is UP"
    else
        echo -e "  ${RED}✗${NC} Product Service is DOWN"
    fi
    
    # Test Inventory Service
    echo -e "${CYAN}Testing Inventory Service...${NC}"
    INVENTORY_STATUS=$(curl -s http://localhost:8082/actuator/health 2>/dev/null | jq -r '.status' || echo "UNAVAILABLE")
    if [ "$INVENTORY_STATUS" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} Inventory Service is UP"
    else
        echo -e "  ${RED}✗${NC} Inventory Service is DOWN"
    fi
    
    # Test Order Service
    echo -e "${CYAN}Testing Order Service...${NC}"
    ORDER_STATUS=$(curl -s http://localhost:8083/actuator/health 2>/dev/null | jq -r '.status' || echo "UNAVAILABLE")
    if [ "$ORDER_STATUS" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} Order Service is UP"
    else
        echo -e "  ${RED}✗${NC} Order Service is DOWN"
    fi
    
    # Test Payment Service
    echo -e "${CYAN}Testing Payment Service...${NC}"
    PAYMENT_STATUS=$(curl -s http://localhost:8084/actuator/health 2>/dev/null | jq -r '.status' || echo "UNAVAILABLE")
    if [ "$PAYMENT_STATUS" == "UP" ]; then
        echo -e "  ${GREEN}✓${NC} Payment Service is UP"
    else
        echo -e "  ${RED}✗${NC} Payment Service is DOWN"
    fi
    
    echo ""
}

# Function to run dummy data script
run_dummy_data() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}6. Populating Test Data${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    if [ -f "./dummyData.sh" ]; then
        echo -e "${YELLOW}Running dummyData.sh to populate test data...${NC}"
        echo ""
        ./dummyData.sh
    else
        echo -e "${YELLOW}dummyData.sh not found, skipping data population${NC}"
    fi
    echo ""
}

# Function to show useful commands
show_commands() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Useful Kubernetes Commands${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "${CYAN}View Logs:${NC}"
    echo "  kubectl logs -f -l app=eureka-server -n microservices-demo"
    echo "  kubectl logs -f -l app=product-service -n microservices-demo"
    echo ""
    echo -e "${CYAN}Scale Services:${NC}"
    echo "  kubectl scale deployment product-service --replicas=3 -n microservices-demo"
    echo ""
    echo -e "${CYAN}Delete Deployment:${NC}"
    echo "  kubectl delete namespace microservices-demo"
    echo ""
    echo -e "${CYAN}Stop Port Forwarding:${NC}"
    echo "  pkill -f 'kubectl port-forward'"
    echo ""
    echo -e "${CYAN}Access Dashboard:${NC}"
    echo "  Eureka: http://localhost:8761"
    echo "  Order Dashboard: http://localhost:8083/dashboard"
    echo ""
}

# Main execution
main() {
    # Parse arguments
    SKIP_BUILD=false
    SKIP_DEPLOY=false
    SKIP_TEST=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            --skip-deploy)
                SKIP_DEPLOY=true
                shift
                ;;
            --skip-test)
                SKIP_TEST=true
                shift
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo ""
                echo "Options:"
                echo "  --skip-build    Skip Docker image building"
                echo "  --skip-deploy   Skip Kubernetes deployment"
                echo "  --skip-test     Skip service testing"
                echo "  --help          Show this help message"
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Execute steps
    check_prerequisites
    
    if [ "$SKIP_BUILD" = false ]; then
        build_images
    fi
    
    if [ "$SKIP_DEPLOY" = false ]; then
        deploy_to_k8s
        check_status
    fi
    
    setup_port_forwarding
    
    if [ "$SKIP_TEST" = false ]; then
        test_services
        run_dummy_data
    fi
    
    show_commands
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ Kubernetes deployment complete!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${CYAN}Your microservices are now running in Kubernetes!${NC}"
    echo ""
}

# Run main function
main "$@"
