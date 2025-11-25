# Payment Service

## Purpose
This microservice handles payment processing for orders. It manages payment transactions, validates payment information, processes refunds, and integrates with payment gateways.

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Client
- Java 17

## Default Configuration
- Port: 8084
- Service Name: payment-service
- Eureka Server: http://localhost:8761/eureka/

## API Endpoints

### Payment Management (CRUD Operations)

#### Process Payment
- **POST** `/payments/process`
- **Description**: Process a new payment
- **Request Body**:
```json
{
  "orderId": "ORDER-001",
  "amount": 450.00,
  "method": "CREDIT_CARD"
}
```
- **Payment Logic**: 
  - Amount (in cents) is even → SUCCESS
  - Amount (in cents) is odd → FAILED

#### Get All Payments
- **GET** `/payments`
- **Description**: Retrieve all payments
- **Response**: Array of payment objects

#### Get Payment by ID
- **GET** `/payments/{id}`
- **Description**: Retrieve a specific payment by ID
- **Response**: Payment object with status

#### Get Payment by Order ID
- **GET** `/payments/order/{orderId}`
- **Description**: Retrieve payment for a specific order
- **Response**: Payment object

#### Update Payment
- **PUT** `/payments/{id}`
- **Description**: Update a payment (cannot update successful payments)
- **Request Body**: Same as Process Payment

#### Delete Payment
- **DELETE** `/payments/{id}`
- **Description**: Delete a payment (cannot delete successful payments)

### Health Check
- **GET** `/actuator/health`
- **Description**: Check service health status

## Inter-Service Communication

### Inbound Calls (Services that call this)
- **Order Service**: Processes payments when orders are being paid

### Payment Processing Logic
The service uses deterministic logic for simulation:
- Converts amount to cents (multiply by 100)
- If cents value is **even** → Payment **SUCCESS**
- If cents value is **odd** → Payment **FAILED**

**Examples**:
- $450.00 → 45000 cents (even) → SUCCESS
- $799.99 → 79999 cents (odd) → FAILED
- $1500.00 → 150000 cents (even) → SUCCESS

## Running the Application
```bash
mvn spring-boot:run
```

## Build
```bash
mvn clean install
```

## Notes
Ensure the Eureka server is running before starting this service.


