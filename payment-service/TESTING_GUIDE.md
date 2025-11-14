# Payment Service - Quick Test Guide

## Testing the Deterministic Logic

The payment service uses a deterministic algorithm where:
- **Even amounts (in cents) → SUCCESS**
- **Odd amounts (in cents) → FAILED**

### Quick Test Scenarios

#### 1. Test SUCCESS (Even Amounts)
```bash
# $100.00 = 10000 cents (even) → SUCCESS
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-001", "amount": 100.00, "method": "CREDIT_CARD"}'

# $10.50 = 1050 cents (even) → SUCCESS
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-002", "amount": 10.50, "method": "DEBIT_CARD"}'

# $0.02 = 2 cents (even) → SUCCESS
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-003", "amount": 0.02, "method": "WALLET"}'
```

#### 2. Test FAILED (Odd Amounts)
```bash
# $99.99 = 9999 cents (odd) → FAILED
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-004", "amount": 99.99, "method": "CREDIT_CARD"}'

# $5.25 = 525 cents (odd) → FAILED
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-005", "amount": 5.25, "method": "PAYPAL"}'

# $0.01 = 1 cent (odd) → FAILED
curl -X POST http://localhost:8084/payments/process \
  -H "Content-Type: application/json" \
  -d '{"orderId": "ORDER-006", "amount": 0.01, "method": "WALLET"}'
```

#### 3. Retrieve Payment
```bash
# Get payment by ID
curl http://localhost:8084/payments/1
```

### Expected Response Format
```json
{
  "id": 1,
  "orderId": "ORDER-001",
  "amount": 100.00,
  "status": "SUCCESS",  // or "FAILED"
  "method": "CREDIT_CARD",
  "createdAt": "2025-11-15T00:00:00.000Z",
  "updatedAt": "2025-11-15T00:00:00.000Z"
}
```

### Run All Unit Tests
```bash
cd payment-service
mvn test
```

Expected output: **19 tests passing** ✅
