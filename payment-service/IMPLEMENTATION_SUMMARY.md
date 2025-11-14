# Payment Service - Implementation Summary

## Overview
The Payment Service simulates payment processing with deterministic logic based on the payment amount. It provides a RESTful API to process payments and retrieve payment information.

## Implementation Details

### Entity: Payment
- **Fields:**
  - `id` (Long) - Primary key, auto-generated
  - `orderId` (String) - Associated order identifier
  - `amount` (BigDecimal) - Payment amount
  - `status` (PaymentStatus enum) - PENDING, SUCCESS, or FAILED
  - `method` (String) - Payment method (e.g., CREDIT_CARD, DEBIT_CARD, PAYPAL)
  - `createdAt` (Instant) - Payment creation timestamp
  - `updatedAt` (Instant) - Last update timestamp

- **Features:**
  - Automatic timestamp management via JPA callbacks
  - Enum-based status tracking
  - Initially set to PENDING status

### Enum: PaymentStatus
- `PENDING` - Initial status
- `SUCCESS` - Payment processed successfully
- `FAILED` - Payment processing failed

### Repository: PaymentRepository
- Extends `JpaRepository<Payment, Long>`
- **Custom Methods:**
  - `findByOrderId(String orderId)` - Find payments by order ID
  - `findByStatus(PaymentStatus status)` - Find payments by status

### Service: PaymentService

#### Deterministic Payment Logic
The service uses a simple, deterministic algorithm to simulate payment processing:

```
Amount in cents = Amount × 100
If (Amount in cents) is EVEN → SUCCESS
If (Amount in cents) is ODD → FAILED
```

**Examples:**
- $100.00 → 10000 cents (even) → ✅ SUCCESS
- $99.99 → 9999 cents (odd) → ❌ FAILED
- $10.50 → 1050 cents (even) → ✅ SUCCESS
- $5.25 → 525 cents (odd) → ❌ FAILED
- $0.02 → 2 cents (even) → ✅ SUCCESS
- $0.01 → 1 cent (odd) → ❌ FAILED

#### Core Methods
- `processPayment(PaymentRequest)` - Process payment with deterministic logic
- `getPaymentById(Long id)` - Retrieve payment by ID

### REST API Endpoints

#### 1. Process Payment
```
POST /payments/process
Body: {
  "orderId": "string",
  "amount": decimal,
  "method": "string"
}
Response: 201 Created
{
  "id": long,
  "orderId": "string",
  "amount": decimal,
  "status": "SUCCESS|FAILED",
  "method": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### 2. Get Payment by ID
```
GET /payments/{id}
Response: 200 OK | 404 Not Found
```

### DTOs

#### PaymentRequest
- `orderId` (String)
- `amount` (BigDecimal)
- `method` (String)

#### PaymentResponse
- `id` (Long)
- `orderId` (String)
- `amount` (BigDecimal)
- `status` (PaymentStatus)
- `method` (String)
- `createdAt` (Instant)
- `updatedAt` (Instant)

### Testing

#### Unit Tests Implemented

**PaymentTest (3 tests)**
- Entity constructor validation
- Payment status enum values
- Getters and setters

**PaymentServiceTest (11 tests)**
- Even amount → SUCCESS outcome
- Odd amount → FAILED outcome
- Small amounts (even and odd)
- Zero amount (even → SUCCESS)
- Large amounts
- Edge cases (0.01, 0.02)
- Payment retrieval by ID
- Not found scenarios
- Request-response mapping

**PaymentControllerTest (5 tests)**
- REST endpoint validation
- HTTP status codes
- Success and failure responses
- Error handling
- Payment retrieval

**Test Results:**
- 19 tests total
- All tests passing ✅
- Covers both SUCCESS and FAILED outcomes
- Tests edge cases and boundary conditions

### Data Flow

**Payment Processing Flow:**
```
POST /payments/process
    ↓
Controller receives PaymentRequest
    ↓
Service creates Payment entity (status: PENDING)
    ↓
Apply deterministic logic:
  - Convert amount to cents (multiply by 100)
  - Check if even or odd
  - Set status: SUCCESS (even) or FAILED (odd)
    ↓
Save payment to database
    ↓
Return PaymentResponse with final status
```

### Configuration
- **Port:** 8084
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
java -jar target/payment-service-1.0.0-SNAPSHOT.jar
```

**Run Tests:**
```bash
mvn test
```

### Example Usage

#### Process Payment - Success (Even Amount)
```bash
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-001",
    "amount": 100.00,
    "method": "CREDIT_CARD"
  }'
```

**Response:**
```json
{
  "id": 1,
  "orderId": "ORDER-001",
  "amount": 100.00,
  "status": "SUCCESS",
  "method": "CREDIT_CARD",
  "createdAt": "2025-11-15T00:00:00Z",
  "updatedAt": "2025-11-15T00:00:00Z"
}
```

#### Process Payment - Failed (Odd Amount)
```bash
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-002",
    "amount": 99.99,
    "method": "DEBIT_CARD"
  }'
```

**Response:**
```json
{
  "id": 2,
  "orderId": "ORDER-002",
  "amount": 99.99,
  "status": "FAILED",
  "method": "DEBIT_CARD",
  "createdAt": "2025-11-15T00:00:00Z",
  "updatedAt": "2025-11-15T00:00:00Z"
}
```

#### Retrieve Payment
```bash
curl http://localhost:8084/payments/1
```

## Deterministic Logic Examples

| Amount   | Cents  | Even/Odd | Status  |
|----------|--------|----------|---------|
| $0.00    | 0      | Even     | SUCCESS |
| $0.01    | 1      | Odd      | FAILED  |
| $0.02    | 2      | Even     | SUCCESS |
| $5.25    | 525    | Odd      | FAILED  |
| $10.50   | 1050   | Even     | SUCCESS |
| $99.99   | 9999   | Odd      | FAILED  |
| $100.00  | 10000  | Even     | SUCCESS |
| $250.00  | 25000  | Even     | SUCCESS |
| $1000.00 | 100000 | Even     | SUCCESS |

## Key Features

✅ Deterministic payment simulation (no external APIs)  
✅ Simple even/odd logic for SUCCESS/FAILED outcomes  
✅ Complete REST API for payment processing  
✅ Payment status tracking (PENDING → SUCCESS/FAILED)  
✅ Support for multiple payment methods  
✅ Comprehensive unit tests (19 tests)  
✅ JPA entity with automatic timestamps  
✅ Service discovery integration  
✅ H2 in-memory database for development  
✅ BigDecimal for precise monetary calculations  

## Notes

- Payment processing is fully deterministic - same amount always produces same result
- No external payment gateway integration (dummy simulation)
- All payments are persisted in the database
- The service can be easily extended to integrate with real payment gateways
- Timestamps are automatically managed by JPA lifecycle callbacks
- BigDecimal is used to avoid floating-point precision issues with monetary values
