#!/bin/bash

# Kubernetes Cleanup Script
# This script removes all deployed resources from Kubernetes

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Kubernetes Cleanup Tool${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Stop all port forwarding
echo -e "${YELLOW}Stopping port forwarding...${NC}"
pkill -f "kubectl port-forward" 2>/dev/null || true
echo -e "${GREEN}✓${NC} Port forwarding stopped"
echo ""

# Delete namespace (this will delete all resources)
echo -e "${YELLOW}Deleting microservices-demo namespace...${NC}"
kubectl delete namespace microservices-demo --ignore-not-found=true

echo ""
echo -e "${YELLOW}Waiting for namespace to be deleted...${NC}"
kubectl wait --for=delete namespace/microservices-demo --timeout=120s 2>/dev/null || true

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✓ Cleanup complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${CYAN}All Kubernetes resources have been removed.${NC}"
echo ""

# Optional: Clean up Docker images
read -p "Do you want to remove Docker images as well? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Removing Docker images...${NC}"
    docker rmi eureka-server:latest eureka-server:v1.0 2>/dev/null || true
    docker rmi product-service:latest product-service:v1.0 2>/dev/null || true
    docker rmi inventory-service:latest inventory-service:v1.0 2>/dev/null || true
    docker rmi order-service:latest order-service:v1.0 2>/dev/null || true
    docker rmi payment-service:latest payment-service:v1.0 2>/dev/null || true
    echo -e "${GREEN}✓${NC} Docker images removed"
fi

echo ""
echo -e "${GREEN}Done!${NC}"
