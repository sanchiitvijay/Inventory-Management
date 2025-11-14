# Order Service - Implementation Summary

## Overview
The Order Service is an orchestration service that coordinates between Product, Payment, and Inventory services to manage the complete order lifecycle from creation to fulfillment. It implements sophisticated transaction management and rollback logic.

## Implementation Details

### Entity Layer

#### Order Entity
- **Fields:**
  - `id` (String/UUID) - Unique order identifier
  - `items` (List<OrderItem>) - List of order items
  - `status` (OrderStatus enum) - Order state
  - `paymentId` (String) - Reference to payment
  - `cancellationReason` (String) - Reason for cancellation
  - `createdAt` (Instant) - Order creation timestamp
  - `updatedAt` (Instant) - Last update timestamp

- **Features:**
  - Auto-generated UUID
  - Embedded collection for order items
  - Automatic timestamp management
  - Total amount calculation

#### OrderItem (Embeddable)
- **Fields:**
  - `productSku` (String) - Product identifier
  - `quantity` (Integer) - Ordered quantity
  - `price` (BigDecimal) - Unit price

- **Methods:**
  - `getTotalPrice()` - Calculates item total (price × quantity)

#### OrderStatus Enum
- `CREATED` - Order created, awaiting payment
- `PAID` - Payment successful, stock deducted
- `FULFILLED` - Order completed and delivered
- `CANCELLED` - Order cancelled (payment failed or insufficient stock)

### Repository Layer
- `OrderRepository` - Spring Data JPA repository
- Custom query: `findByStatus(OrderStatus status)`

### Service Layer: OrderService

#### Core Orchestration Logic

**1. Create Order Flow:**
```
POST /orders
    ↓
Validate products exist (call product-service)
    ↓
Create order with status CREATED
    ↓
Persist order
    ↓
Return order response
```

**2. Pay Order Flow (Happy Path):**
```
POST /orders/{id}/pay
    ↓
Validate order status (must be CREATED)
    ↓
Process payment (call payment-service)
    ↓
Payment SUCCESS?
    ↓ YES
Check inventory availability
    ↓
Stock available?
    ↓ YES
Deduct stock (call inventory-service)
    ↓
Update order status to PAID
    ↓
Return order response
```

**3. Pay Order Flow (Failure Path - Insufficient Stock):**
```
POST /orders/{id}/pay
    ↓
Process payment → SUCCESS
    ↓
Check inventory availability
    ↓
Stock INSUFFICIENT
    ↓
Cancel order (no rollback to payment service in this implementation)
    ↓
Update order status to CANCELLED
    ↓
Set cancellation reason: "Insufficient inventory to fulfill order"
    ↓
Return order response
```

**4. Pay Order Flow (Failure Path - Payment Failed):**
```
POST /orders/{id}/pay
    ↓
Process payment → FAILED
    ↓
Keep order status as CREATED
    ↓
Set cancellation reason: "Payment failed"
    ↓
Return order response
```

### REST API Endpoints

#### 1. Create Order
```
POST /orders
Body: {
  "items": [
    {
      "productSku": "string",
      "quantity": integer,
      "price": decimal
    }
  ]
}
Response: 201 Created
```

**Business Logic:**
- Validates each product SKU exists via product-service
- Creates order with status CREATED
- Returns order with generated UUID

#### 2. Pay Order
```
POST /orders/{id}/pay
Response: 200 OK
```

**Business Logic:**
- Validates order exists and status is CREATED
- Processes payment via payment-service
- If payment SUCCESS:
  - Checks inventory availability
  - If stock available: deducts stock and sets status to PAID
  - If stock insufficient: sets status to CANCELLED
- If payment FAILED: keeps status as CREATED with reason

#### 3. Get Order by ID
```
GET /orders/{id}
Response: 200 OK | 404 Not Found
```

#### 4. Get All Orders
```
GET /orders
Response: 200 OK
```

### Client Integration

#### Product Service Integration
- **Endpoint:** `GET /products/sku/{sku}`
- **Purpose:** Validate product exists during order creation
- **Response:** ProductResponse with product details

#### Payment Service Integration
- **Endpoint:** `POST /payments/process`
- **Purpose:** Process payment for order total
- **Request:** PaymentRequest with orderId, amount, method
- **Response:** PaymentResponse with payment status (SUCCESS/FAILED)

#### Inventory Service Integration
- **Endpoint:** `GET /inventory/{sku}` - Check availability
- **Endpoint:** `PUT /inventory/{sku}` - Deduct stock
- **Purpose:** Verify stock and deduct quantities
- **Logic:** 
  - Check if available >= ordered quantity
  - If yes, update with new available = current - ordered
  - If no, cancel order

### Configuration
- **Port:** 8083
- **Database:** H2 in-memory database
- **Service Discovery:** Eureka-enabled with load balancing
- **RestTemplate:** Load-balanced for service-to-service communication

### Testing

#### Unit Tests Implemented

**OrderTest (7 tests)**
- Entity creation and UUID generation
- Total amount calculation
- Order status enum validation
- Order item total price calculation
- Getters and setters

**OrderServiceTest (11 tests)**
- ✅ **Happy Path:** Create order → Pay → Stock deduction → PAID status
- ❌ **Failure Path:** Insufficient stock → Order CANCELLED
- ❌ **Failure Path:** Payment failed → Order remains CREATED
- Product validation
- Order not found scenarios
- Order already paid scenarios
- Multiple items with stock validation
- Get order operations

**OrderControllerTest (7 tests)**
- REST endpoint validation
- HTTP status codes
- Request/response mapping
- Error handling

**Test Results:**
- 25 tests total
- All tests passing ✅
- Covers happy path (order → pay → stock deduction)
- Covers failure paths (insufficient stock, payment failure)

### Data Flow Examples

#### Example 1: Successful Order Flow
```
1. POST /orders
   Items: [LAPTOP-001 x2 @ $1000]
   → Validates LAPTOP-001 exists in product-service
   → Creates order with CREATED status
   → Returns order-123

2. POST /orders/order-123/pay
   → Calls payment-service with $2000
   → Payment returns SUCCESS (amount is even)
   → Checks inventory: LAPTOP-001 has 100 available
   → Deducts 2 units: PUT /inventory/LAPTOP-001 {available: 98}
   → Updates order status to PAID
   → Returns order with PAID status
```

#### Example 2: Insufficient Stock Flow
```
1. POST /orders
   Items: [LAPTOP-001 x200 @ $1000]
   → Creates order-456 with CREATED status

2. POST /orders/order-456/pay
   → Calls payment-service with $200,000
   → Payment returns SUCCESS
   → Checks inventory: LAPTOP-001 has only 100 available
   → Available (100) < Quantity (200) → INSUFFICIENT
   → Updates order status to CANCELLED
   → Sets reason: "Insufficient inventory to fulfill order"
   → Returns order with CANCELLED status
```

#### Example 3: Payment Failure Flow
```
1. POST /orders
   Items: [LAPTOP-001 x1 @ $999.99]
   → Creates order-789 with CREATED status

2. POST /orders/order-789/pay
   → Calls payment-service with $999.99
   → Payment returns FAILED (odd amount in cents)
   → Keeps order status as CREATED
   → Sets reason: "Payment failed"
   → Returns order with CREATED status and failure reason
```

### Transaction Management

#### Success Scenario
1. Order created (persisted)
2. Payment processed (external service)
3. Inventory checked (external service)
4. Stock deducted (external service)
5. Order updated to PAID (persisted)

#### Rollback Scenarios

**Insufficient Stock:**
- Payment already succeeded
- Stock deduction fails
- Order marked as CANCELLED
- **Note:** Payment is NOT reversed (limitation - would need compensating transaction)

**Payment Failure:**
- Payment fails immediately
- No inventory operations performed
- Order remains in CREATED state

### Key Business Rules

1. **Product Validation:** All products must exist before order creation
2. **Single Payment:** Order can only be paid once (must be in CREATED status)
3. **Atomic Stock Deduction:** All items must have sufficient stock or order is cancelled
4. **No Partial Fulfillment:** Order is all-or-nothing
5. **Payment First:** Payment is processed before inventory check
6. **Idempotency:** Order ID is UUID, ensuring uniqueness

### Dependencies
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Web
- Spring Cloud Netflix Eureka Client
- Spring Cloud LoadBalancer
- H2 Database
- RestTemplate for service communication
- Mockito 5.14.2 (testing)
- ByteBuddy 1.15.11 (testing)

### Build & Run

**Build:**
```bash
mvn clean package
```

**Run:**
```bash
java -jar target/order-service-1.0.0-SNAPSHOT.jar
```

**Run Tests:**
```bash
mvn test
```

### Example API Usage

#### Create Order
```bash
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productSku": "LAPTOP-001",
        "quantity": 2,
        "price": 1000.00
      }
    ]
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "productSku": "LAPTOP-001",
      "quantity": 2,
      "price": 1000.00
    }
  ],
  "status": "CREATED",
  "totalAmount": 2000.00,
  "paymentId": null,
  "cancellationReason": null,
  "createdAt": "2025-11-15T00:00:00Z",
  "updatedAt": "2025-11-15T00:00:00Z"
}
```

#### Pay for Order
```bash
curl -X POST http://localhost:8083/orders/550e8400-e29b-41d4-a716-446655440000/pay
```

**Response (Success):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PAID",
  "paymentId": "123",
  "totalAmount": 2000.00
}
```

**Response (Insufficient Stock):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CANCELLED",
  "paymentId": "123",
  "cancellationReason": "Insufficient inventory to fulfill order"
}
```

## Key Features

✅ UUID-based order identification  
✅ Order orchestration across multiple services  
✅ Product validation via product-service  
✅ Payment processing via payment-service  
✅ Stock validation and deduction via inventory-service  
✅ Automatic cancellation on insufficient stock  
✅ Order status lifecycle management  
✅ Comprehensive unit tests (25 tests)  
✅ Happy path testing (create → pay → fulfill)  
✅ Failure path testing (insufficient stock, payment failure)  
✅ Load-balanced service communication  
✅ RESTful API design  

## Architecture Notes

### Saga Pattern (Simplified)
This service implements a simplified orchestration-based saga pattern:
- Orchestrator: OrderService
- Participants: ProductService, PaymentService, InventoryService
- Compensation: Limited (order cancellation, no payment reversal)

### Future Enhancements
1. **Payment Reversal:** Implement compensating transaction to refund payment when stock is insufficient
2. **Retry Logic:** Add retry mechanisms for transient failures
3. **Event Sourcing:** Track all state changes as events
4. **Distributed Tracing:** Add correlation IDs for request tracking
5. **Circuit Breaker:** Prevent cascading failures
6. **Async Processing:** Use message queues for order processing
