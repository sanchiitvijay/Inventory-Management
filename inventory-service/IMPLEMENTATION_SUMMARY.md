# Inventory Service - Implementation Summary

## Overview
The Inventory Service manages inventory items with low-stock detection and event logging capabilities.

## Implementation Details

### Entity: InventoryItem
- **Fields:**
  - `id` (Long) - Primary key, auto-generated
  - `productSku` (String) - Unique product identifier
  - `available` (int) - Available quantity
  - `threshold` (int) - Low-stock threshold
  - `lastUpdated` (Instant) - Last update timestamp

- **Features:**
  - Automatic timestamp updates via JPA callbacks (`@PrePersist`, `@PreUpdate`)
  - Low-stock detection method: `isLowStock()` returns true when `available <= threshold`

### Repository: InventoryRepository
- Extends `JpaRepository<InventoryItem, Long>`
- **Custom Methods:**
  - `findByProductSku(String productSku)` - Find item by SKU
  - `findLowStockItems()` - Query for items where available <= threshold

### Service: InventoryService
- **Core Methods:**
  - `getInventoryBySku(String sku)` - Retrieve inventory by SKU
  - `createInventoryItem(InventoryItemRequest)` - Create new inventory item
  - `updateInventoryItem(String sku, InventoryItemRequest)` - Update existing item
  - `deductStock(String sku, int quantity)` - Deduct stock with validation
  - `getLowStockItems()` - List all items below threshold
  - `getEventLog()` - Retrieve low-stock event history

- **Business Logic:**
  - Validates stock availability before deduction
  - Automatically triggers low-stock events when appropriate
  - Prevents duplicate SKU creation
  - Transactional operations for data consistency

### REST API Endpoints

#### 1. Get Inventory by SKU
```
GET /inventory/{sku}
Response: 200 OK | 404 Not Found
```

#### 2. Create Inventory Item
```
POST /inventory
Body: {
  "productSku": "string",
  "available": integer,
  "threshold": integer
}
Response: 201 Created | 400 Bad Request
```

#### 3. Update Inventory Item
```
PUT /inventory/{sku}
Body: {
  "available": integer (optional),
  "threshold": integer (optional)
}
Response: 200 OK | 404 Not Found
```

#### 4. Get Low-Stock Items
```
GET /inventory/low-stock
Response: 200 OK
Returns list of items where available <= threshold
```

#### 5. Get Event Log
```
GET /inventory/events
Response: 200 OK
Returns list of low-stock events with timestamps
```

### Event System

#### EventLogger
- In-memory event log (thread-safe ArrayList)
- Records low-stock events with:
  - Product SKU
  - Available quantity
  - Threshold value
  - Timestamp

#### LowStockEvent
- Immutable event object
- Created whenever an operation results in low stock
- Logged automatically by the service layer

### Testing

#### Unit Tests Implemented

**InventoryItemTest (6 tests)**
- Low-stock detection (below, at, above threshold)
- Constructor and getter/setter validation
- Null value handling

**InventoryServiceTest (16 tests)**
- Stock deduction with various scenarios
- Low-stock event triggering
- Insufficient stock validation
- CRUD operations
- Empty list handling
- Error conditions

**InventoryControllerTest (8 tests)**
- REST endpoint validation
- HTTP status code verification
- Request/response mapping
- Error handling

**Test Results:**
- 30 tests total
- All tests passing ✅
- Covers stock deduction logic
- Covers low-stock detection logic

### Data Flow

1. **Stock Deduction Flow:**
   ```
   API Request → Controller → Service
   ├─ Validate stock availability
   ├─ Deduct quantity
   ├─ Save to database
   └─ Check low-stock condition
      └─ Log event if low stock detected
   ```

2. **Low-Stock Detection:**
   ```
   After any operation that modifies `available`:
   ├─ Check: available <= threshold
   └─ If true: Create and log LowStockEvent
   ```

### Configuration
- **Port:** 8082
- **Database:** H2 in-memory database
- **JPA:** Auto-update DDL
- **Service Discovery:** Registered with Eureka at `http://eureka-server:8761/eureka/`

### Dependencies
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Web
- H2 Database
- Spring Cloud Netflix Eureka Client
- Mockito 5.14.2 (for testing)
- ByteBuddy 1.15.11 (for testing)

### Build & Run

**Build:**
```bash
mvn clean package
```

**Run:**
```bash
java -jar target/inventory-service-1.0.0-SNAPSHOT.jar
```

**Run Tests:**
```bash
mvn test
```

### Example Usage

**Create Inventory:**
```bash
curl -X POST http://localhost:8082/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productSku": "LAPTOP-001",
    "available": 100,
    "threshold": 20
  }'
```

**Deduct Stock (via Service):**
```java
inventoryService.deductStock("LAPTOP-001", 85);
// This will trigger a low-stock event (100 - 85 = 15 <= 20)
```

**Get Low-Stock Items:**
```bash
curl http://localhost:8082/inventory/low-stock
```

**View Event Log:**
```bash
curl http://localhost:8082/inventory/events
```

## Key Features

✅ Complete CRUD operations for inventory items  
✅ Automatic low-stock detection (available <= threshold)  
✅ In-memory event logging for audit trail  
✅ Stock deduction with validation  
✅ RESTful API endpoints  
✅ Comprehensive unit tests (30 tests)  
✅ JPA entity with automatic timestamps  
✅ Service discovery integration  
✅ H2 in-memory database for development  

## Notes

- The event log is in-memory and will reset on service restart
- Low-stock events are logged immediately when detected
- All database operations are transactional
- The service validates data at multiple layers (entity, service, controller)
