package com.microservices.order.client;

import java.math.BigDecimal;

public class PaymentRequest {

    private String orderId;
    private BigDecimal amount;
    private String method;

    public PaymentRequest() {
    }

    public PaymentRequest(String orderId, BigDecimal amount, String method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
