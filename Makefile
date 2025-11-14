.PHONY: help build up down logs clean test restart rebuild ps

# Default target
help:
	@echo "Docker Compose Management Commands:"
	@echo ""
	@echo "  make build      - Build all Docker images"
	@echo "  make up         - Start all services"
	@echo "  make down       - Stop all services"
	@echo "  make logs       - View logs (all services)"
	@echo "  make logs-f     - Follow logs (all services)"
	@echo "  make clean      - Stop and remove containers, networks, volumes"
	@echo "  make restart    - Restart all services"
	@echo "  make rebuild    - Rebuild and restart all services"
	@echo "  make ps         - List running containers"
	@echo "  make test       - Test the setup"
	@echo ""
	@echo "Service-specific commands:"
	@echo "  make logs-<service>    - View logs for specific service"
	@echo "  make restart-<service> - Restart specific service"
	@echo "  make rebuild-<service> - Rebuild specific service"
	@echo ""
	@echo "Examples:"
	@echo "  make logs-order-service"
	@echo "  make restart-order-service"
	@echo "  make rebuild-order-service"

# Build all images
build:
	docker-compose build

# Start services
up:
	docker-compose up -d
	@echo "Services starting... Wait 60 seconds for full startup"
	@echo "Eureka Dashboard: http://localhost:8761"
	@echo "Order Dashboard: http://localhost:8083/dashboard"

# Start with logs
up-logs:
	docker-compose up

# Stop services
down:
	docker-compose down

# View logs
logs:
	docker-compose logs

# Follow logs
logs-f:
	docker-compose logs -f

# Clean everything
clean:
	docker-compose down -v --remove-orphans
	@echo "Cleaned up containers, networks, and volumes"

# Restart all services
restart:
	docker-compose restart

# Rebuild and restart all services
rebuild:
	docker-compose up -d --build
	@echo "Services rebuilt and restarted"

# Show running containers
ps:
	docker-compose ps

# Test the setup
test:
	@echo "Testing Docker setup..."
	@echo "Checking if services are running..."
	@docker-compose ps
	@echo ""
	@echo "Testing Eureka (wait a few seconds if just started)..."
	@curl -s http://localhost:8761 > /dev/null && echo "✓ Eureka is accessible" || echo "✗ Eureka not accessible"
	@echo ""
	@echo "Testing Order Service..."
	@curl -s http://localhost:8083/dashboard > /dev/null && echo "✓ Order Dashboard is accessible" || echo "✗ Order Dashboard not accessible"

# Service-specific logs
logs-eureka:
	docker-compose logs -f eureka-server

logs-product:
	docker-compose logs -f product-service

logs-inventory:
	docker-compose logs -f inventory-service

logs-order:
	docker-compose logs -f order-service

logs-payment:
	docker-compose logs -f payment-service

# Service-specific restart
restart-eureka:
	docker-compose restart eureka-server

restart-product:
	docker-compose restart product-service

restart-inventory:
	docker-compose restart inventory-service

restart-order:
	docker-compose restart order-service

restart-payment:
	docker-compose restart payment-service

# Service-specific rebuild
rebuild-eureka:
	docker-compose up -d --build eureka-server

rebuild-product:
	docker-compose up -d --build product-service

rebuild-inventory:
	docker-compose up -d --build inventory-service

rebuild-order:
	docker-compose up -d --build order-service

rebuild-payment:
	docker-compose up -d --build payment-service

# Production deployment
prod-up:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
	@echo "Production services starting..."

prod-down:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

# Network inspection
network:
	docker network inspect microservices-network

# Open dashboards
open-eureka:
	@open http://localhost:8761 || xdg-open http://localhost:8761 2>/dev/null || echo "Please open http://localhost:8761"

open-dashboard:
	@open http://localhost:8083/dashboard || xdg-open http://localhost:8083/dashboard 2>/dev/null || echo "Please open http://localhost:8083/dashboard"
