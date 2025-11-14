package com.microservices.payment.dto;

import com.microservices.payment.entity.Payment;
import com.microservices.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String method;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.orderId = payment.getOrderId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
        this.method = payment.getMethod();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
