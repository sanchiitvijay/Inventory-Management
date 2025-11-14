package com.microservices.payment.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void testPaymentConstructor() {
        Payment payment = new Payment("ORDER-001", new BigDecimal("100.00"), "CREDIT_CARD");

        assertEquals("ORDER-001", payment.getOrderId());
        assertEquals(new BigDecimal("100.00"), payment.getAmount());
        assertEquals("CREDIT_CARD", payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());
    }

    @Test
    void testPaymentStatusEnum() {
        assertEquals(3, PaymentStatus.values().length);
        assertNotNull(PaymentStatus.valueOf("PENDING"));
        assertNotNull(PaymentStatus.valueOf("SUCCESS"));
        assertNotNull(PaymentStatus.valueOf("FAILED"));
    }

    @Test
    void testPaymentGettersAndSetters() {
        Payment payment = new Payment();

        payment.setId(1L);
        payment.setOrderId("ORDER-100");
        payment.setAmount(new BigDecimal("250.50"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMethod("PAYPAL");

        assertEquals(1L, payment.getId());
        assertEquals("ORDER-100", payment.getOrderId());
        assertEquals(new BigDecimal("250.50"), payment.getAmount());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("PAYPAL", payment.getMethod());
    }
}
