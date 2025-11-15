#!/bin/bash

# Microservices Dummy Data Script
# This script populates all microservices with test data and retrieves it for verification
#
# ============================================
# ALL AVAILABLE ENDPOINTS BY SERVICE
# ============================================
#
# PRODUCT SERVICE (Port 8081)
# ---------------------------
# POST   /products              - Create a new product
# GET    /products              - Get all products
# GET    /products/{id}         - Get product by ID
# GET    /products/sku/{sku}    - Get product by SKU
# PUT    /products/{id}         - Update product by ID
# DELETE /products/{id}         - Delete product by ID
# GET    /actuator/health       - Health check
#
# INVENTORY SERVICE (Port 8082)
# -----------------------------
# GET    /inventory/{sku}       - Get inventory by SKU
# POST   /inventory             - Create inventory for a product
# PUT    /inventory/{sku}       - Update inventory (add/remove stock)
# GET    /inventory/low-stock   - Get all low stock items
# GET    /inventory/events      - Get all inventory events (SSE - Server-Sent Events)
# GET    /inventory/alerts      - Get all low stock alerts
# GET    /inventory/alerts/{sku} - Get low stock alerts by SKU
# GET    /actuator/health       - Health check
#
# ORDER SERVICE (Port 8083)
# -------------------------
# POST   /orders                - Create a new order
# POST   /orders/{id}/pay       - Process payment for an order
# GET    /orders/{id}           - Get order by ID
# GET    /orders                - Get all orders
# GET    /dashboard             - Order dashboard (HTML UI)
# GET    /actuator/health       - Health check
#
# PAYMENT SERVICE (Port 8084)
# ---------------------------
# POST   /payments/process      - Process a payment
# GET    /payments/{id}         - Get payment details by ID
# GET    /actuator/health       - Health check
#
# EUREKA SERVER (Port 8761)
# -------------------------
# GET    /                      - Eureka dashboard
# GET    /actuator/health       - Health check
#
# ============================================

set -e  # Exit on error

# Output file for storing results
OUTPUT_FILE="output.txt"

# Clear output file and add header
echo "Microservices Dummy Data Population Output" > "$OUTPUT_FILE"
echo "Generated on: $(date)" >> "$OUTPUT_FILE"
echo "============================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Service URLs
PRODUCT_SERVICE="http://localhost:8081"
INVENTORY_SERVICE="http://localhost:8082"
ORDER_SERVICE="http://localhost:8083"
PAYMENT_SERVICE="http://localhost:8084"
EUREKA_SERVER="http://localhost:8761"

echo -e "${CYAN}======================================${NC}"
echo -e "${CYAN}  Microservices Data Population Tool${NC}"
echo -e "${CYAN}======================================${NC}"
echo ""

# Function to check if services are running
check_services() {
    echo -e "${YELLOW}Checking if all services are running...${NC}"
    
    services=("8761:Eureka" "8081:Product" "8082:Inventory" "8083:Order" "8084:Payment")
    all_running=true
    
    for service in "${services[@]}"; do
        port="${service%%:*}"
        name="${service##*:}"
        
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} $name Service (port $port) is running"
        else
            echo -e "  ${RED}✗${NC} $name Service (port $port) is NOT running"
            all_running=false
        fi
    done
    
    echo ""
    
    if [ "$all_running" = false ]; then
        echo -e "${RED}Error: Not all services are running. Please start all services first.${NC}"
        exit 1
    fi
}

# Function to wait a bit between requests
wait_between_requests() {
    sleep 0.5
}

# Check services before starting
check_services

# ============================================
# PRODUCT SERVICE - Create Products
# ============================================
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}1. Creating Products${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Create Product 1 - Laptop
echo -e "${MAGENTA}Creating Product: Laptop${NC}"
echo "Creating Product: Laptop" >> "$OUTPUT_FILE"
PRODUCT1=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dell XPS 15",
    "sku": "LAPTOP-XPS15",
    "description": "High-performance laptop with Intel i7 processor",
    "recommendedRetailPrice": 1299.99
  }')
echo "$PRODUCT1" | jq . | tee -a "$OUTPUT_FILE"
PRODUCT1_ID=$(echo "$PRODUCT1" | jq -r '.id')
wait_between_requests

# Create Product 2 - Mouse
echo -e "${MAGENTA}Creating Product: Wireless Mouse${NC}"
PRODUCT2=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Logitech MX Master 3",
    "sku": "MOUSE-MX3",
    "description": "Ergonomic wireless mouse for professionals",
    "recommendedRetailPrice": 99.99
  }')
echo "$PRODUCT2" | jq .
PRODUCT2_ID=$(echo "$PRODUCT2" | jq -r '.id')
wait_between_requests

# Create Product 3 - Keyboard
echo -e "${MAGENTA}Creating Product: Mechanical Keyboard${NC}"
PRODUCT3=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Keychron K2 Mechanical Keyboard",
    "sku": "KEYBOARD-K2",
    "description": "Wireless mechanical keyboard with RGB lighting",
    "recommendedRetailPrice": 89.99
  }')
echo "$PRODUCT3" | jq .
PRODUCT3_ID=$(echo "$PRODUCT3" | jq -r '.id')
wait_between_requests

# Create Product 4 - Monitor
echo -e "${MAGENTA}Creating Product: 4K Monitor${NC}"
PRODUCT4=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "LG UltraFine 27UN850",
    "sku": "MONITOR-LG27",
    "description": "27-inch 4K UHD monitor with USB-C",
    "recommendedRetailPrice": 549.99
  }')
echo "$PRODUCT4" | jq .
PRODUCT4_ID=$(echo "$PRODUCT4" | jq -r '.id')
wait_between_requests

# Create Product 5 - Webcam
echo -e "${MAGENTA}Creating Product: HD Webcam${NC}"
PRODUCT5=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Logitech C920 HD Pro",
    "sku": "WEBCAM-C920",
    "description": "1080p HD webcam with stereo audio",
    "recommendedRetailPrice": 79.99
  }')
echo "$PRODUCT5" | jq .
PRODUCT5_ID=$(echo "$PRODUCT5" | jq -r '.id')
wait_between_requests

# Create Product 6 - Headphones
echo -e "${MAGENTA}Creating Product: Noise-Cancelling Headphones${NC}"
PRODUCT6=$(curl -s -X POST $PRODUCT_SERVICE/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sony WH-1000XM4",
    "sku": "HEADPHONES-SONY",
    "description": "Premium noise-cancelling wireless headphones",
    "recommendedRetailPrice": 349.99
  }')
echo "$PRODUCT6" | jq .
PRODUCT6_ID=$(echo "$PRODUCT6" | jq -r '.id')

echo -e "${GREEN}✓ Created 6 products${NC}"
echo ""

# ============================================
# INVENTORY SERVICE - Create Inventory
# ============================================
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}2. Creating Inventory${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Inventory for Laptop - Normal stock
echo -e "${MAGENTA}Creating Inventory: Laptop (Normal Stock)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "LAPTOP-XPS15",
    "available": 25,
    "threshold": 5
  }' | jq .
wait_between_requests

# Inventory for Mouse - Low stock
echo -e "${MAGENTA}Creating Inventory: Mouse (Low Stock - Alert!)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "MOUSE-MX3",
    "available": 3,
    "threshold": 10
  }' | jq .
wait_between_requests

# Inventory for Keyboard - Normal stock
echo -e "${MAGENTA}Creating Inventory: Keyboard (High Stock)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "KEYBOARD-K2",
    "available": 150,
    "threshold": 20
  }' | jq .
wait_between_requests

# Inventory for Monitor - Normal stock
echo -e "${MAGENTA}Creating Inventory: Monitor (Normal Stock)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "MONITOR-LG27",
    "available": 15,
    "threshold": 5
  }' | jq .
wait_between_requests

# Inventory for Webcam - Low stock
echo -e "${MAGENTA}Creating Inventory: Webcam (Low Stock - Alert!)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "WEBCAM-C920",
    "available": 2,
    "threshold": 8
  }' | jq .
wait_between_requests

# Inventory for Headphones - Normal stock
echo -e "${MAGENTA}Creating Inventory: Headphones (Normal Stock)${NC}"
curl -s -X POST $INVENTORY_SERVICE/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "HEADPHONES-SONY",
    "available": 40,
    "threshold": 10
  }' | jq .

echo -e "${GREEN}✓ Created 6 inventory items${NC}"
echo ""

# ============================================
# ORDER SERVICE - Create Orders
# ============================================
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}3. Creating Orders${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Order 1 - Laptop order
echo -e "${MAGENTA}Creating Order: Laptop (2 units)${NC}"
ORDER1=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "LAPTOP-XPS15",
    "quantity": 2,
    "price": 1299.99
  }')
echo "$ORDER1" | jq .
wait_between_requests

# Order 2 - Mouse order
echo -e "${MAGENTA}Creating Order: Wireless Mouse (5 units)${NC}"
ORDER2=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "MOUSE-MX3",
    "quantity": 5,
    "price": 99.99
  }')
echo "$ORDER2" | jq .
wait_between_requests

# Order 3 - Keyboard order
echo -e "${MAGENTA}Creating Order: Mechanical Keyboard (3 units)${NC}"
ORDER3=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "KEYBOARD-K2",
    "quantity": 3,
    "price": 89.99
  }')
echo "$ORDER3" | jq .
wait_between_requests

# Order 4 - Monitor order
echo -e "${MAGENTA}Creating Order: 4K Monitor (1 unit)${NC}"
ORDER4=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "MONITOR-LG27",
    "quantity": 1,
    "price": 549.99
  }')
echo "$ORDER4" | jq .
wait_between_requests

# Order 5 - Headphones order
echo -e "${MAGENTA}Creating Order: Headphones (4 units)${NC}"
ORDER5=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "HEADPHONES-SONY",
    "quantity": 4,
    "price": 349.99
  }')
echo "$ORDER5" | jq .
wait_between_requests

# Order 6 - Another Keyboard order
echo -e "${MAGENTA}Creating Order: Mechanical Keyboard (10 units)${NC}"
ORDER6=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "KEYBOARD-K2",
    "quantity": 10,
    "price": 89.99
  }')
echo "$ORDER6" | jq .
wait_between_requests

# Order 7 - Webcam order
echo -e "${MAGENTA}Creating Order: HD Webcam (2 units)${NC}"
ORDER7=$(curl -s -X POST $ORDER_SERVICE/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "WEBCAM-C920",
    "quantity": 2,
    "price": 79.99
  }')
echo "$ORDER7" | jq .

echo -e "${GREEN}✓ Created 7 orders${NC}"
echo ""

# ============================================
# PAYMENT SERVICE - Process Payments
# ============================================
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}4. Processing Test Payments${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Payment 1 - Success (amount <= 1000)
echo -e "${MAGENTA}Processing Payment: Successful (Amount: $450.00)${NC}"
PAYMENT1=$(curl -s -X POST $PAYMENT_SERVICE/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-001",
    "amount": 450.00,
    "method": "CREDIT_CARD"
  }')
echo "$PAYMENT1" | jq .
wait_between_requests

# Payment 2 - Success
echo -e "${MAGENTA}Processing Payment: Successful (Amount: $799.99)${NC}"
PAYMENT2=$(curl -s -X POST $PAYMENT_SERVICE/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-002",
    "amount": 799.99,
    "method": "DEBIT_CARD"
  }')
echo "$PAYMENT2" | jq .
wait_between_requests

# Payment 3 - Failed (amount > 1000)
echo -e "${MAGENTA}Processing Payment: Failed (Amount: $1500.00)${NC}"
PAYMENT3=$(curl -s -X POST $PAYMENT_SERVICE/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-003",
    "amount": 1500.00,
    "method": "CREDIT_CARD"
  }')
echo "$PAYMENT3" | jq .
wait_between_requests

# Payment 4 - Success
echo -e "${MAGENTA}Processing Payment: Successful (Amount: $189.98)${NC}"
PAYMENT4=$(curl -s -X POST $PAYMENT_SERVICE/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-004",
    "amount": 189.98,
    "method": "PAYPAL"
  }')
echo "$PAYMENT4" | jq .

echo -e "${GREEN}✓ Processed 4 test payments${NC}"
echo ""

# ============================================
# RETRIEVING DATA - Verification
# ============================================
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}5. Retrieving All Data (Verification)${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Get all products
echo -e "${CYAN}━━━ All Products ━━━${NC}"
curl -s $PRODUCT_SERVICE/products | jq .
echo ""

# Get specific product
echo -e "${CYAN}━━━ Product Details (ID: $PRODUCT1_ID) ━━━${NC}"
curl -s $PRODUCT_SERVICE/products/$PRODUCT1_ID | jq .
echo ""

# Get all inventory (by checking each SKU)
echo -e "${CYAN}━━━ Inventory for LAPTOP-XPS15 ━━━${NC}"
curl -s $INVENTORY_SERVICE/inventory/LAPTOP-XPS15 | jq .
echo ""

echo -e "${CYAN}━━━ Inventory for MOUSE-MX3 ━━━${NC}"
curl -s $INVENTORY_SERVICE/inventory/MOUSE-MX3 | jq .
echo ""

echo -e "${CYAN}━━━ Inventory for KEYBOARD-K2 ━━━${NC}"
curl -s $INVENTORY_SERVICE/inventory/KEYBOARD-K2 | jq .
echo ""

# Get low stock items
echo -e "${CYAN}━━━ Low Stock Items ━━━${NC}"
LOW_STOCK=$(curl -s $INVENTORY_SERVICE/inventory/low-stock)
echo "$LOW_STOCK" | jq .
LOW_STOCK_COUNT=$(echo "$LOW_STOCK" | jq 'length')
echo -e "${YELLOW}Found $LOW_STOCK_COUNT items with low stock${NC}"
echo ""

# Get low stock alerts
echo -e "${CYAN}━━━ Low Stock Alerts (Persisted) ━━━${NC}"
ALERTS=$(curl -s $INVENTORY_SERVICE/inventory/alerts)
echo "$ALERTS" | jq .
ALERT_COUNT=$(echo "$ALERTS" | jq 'length')
echo -e "${YELLOW}Found $ALERT_COUNT low stock alerts${NC}"
echo ""

# Get all orders
echo -e "${CYAN}━━━ All Orders ━━━${NC}"
ALL_ORDERS=$(curl -s $ORDER_SERVICE/orders)
echo "$ALL_ORDERS" | jq .
ORDER_COUNT=$(echo "$ALL_ORDERS" | jq 'length')
echo -e "${YELLOW}Found $ORDER_COUNT orders${NC}"
echo ""

# Get payment details
echo -e "${CYAN}━━━ Payment Details (ID: 1) ━━━${NC}"
curl -s $PAYMENT_SERVICE/payments/1 | jq .
echo ""

echo -e "${CYAN}━━━ Payment Details (ID: 3 - Failed) ━━━${NC}"
curl -s $PAYMENT_SERVICE/payments/3 | jq .
echo ""

# Health checks
echo -e "${CYAN}━━━ Service Health Status ━━━${NC}"
echo -e "Eureka Server: $(curl -s $EUREKA_SERVER/actuator/health | jq -r '.status')"
echo -e "Product Service: $(curl -s $PRODUCT_SERVICE/actuator/health | jq -r '.status')"
echo -e "Inventory Service: $(curl -s $INVENTORY_SERVICE/actuator/health | jq -r '.status')"
echo -e "Order Service: $(curl -s $ORDER_SERVICE/actuator/health | jq -r '.status')"
echo -e "Payment Service: $(curl -s $PAYMENT_SERVICE/actuator/health | jq -r '.status')"
echo ""

# ============================================
# SUMMARY
# ============================================
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}✓ Data Population Complete!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${CYAN}Summary:${NC}"
echo -e "  • Created ${GREEN}6 products${NC}"
echo -e "  • Created ${GREEN}6 inventory items${NC}"
echo -e "  • Created ${GREEN}7 orders${NC}"
echo -e "  • Detected ${YELLOW}$LOW_STOCK_COUNT low stock items${NC}"
echo -e "  • Generated ${YELLOW}$ALERT_COUNT low stock alerts${NC}"
echo -e "  • Processed ${GREEN}4 payments${NC} (3 success, 1 failed)"
echo ""
echo -e "${CYAN}Access Points:${NC}"
echo -e "  • Eureka Dashboard: ${BLUE}http://localhost:8761${NC}"
echo -e "  • Order Dashboard:  ${BLUE}http://localhost:8083/dashboard${NC}"
echo -e "  • Product API:      ${BLUE}http://localhost:8081/products${NC}"
echo -e "  • Inventory API:    ${BLUE}http://localhost:8082/inventory${NC}"
echo -e "  • Payment API:      ${BLUE}http://localhost:8084/payments${NC}"
echo ""
echo -e "${GREEN}All services populated with dummy data successfully! 🎉${NC}"
