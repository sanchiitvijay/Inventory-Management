package com.microservices.order.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testOrderConstructor_GeneratesUUID() {
        Order order = new Order();

        assertNotNull(order.getId());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void testOrderConstructorWithItems() {
        OrderItem item1 = new OrderItem("LAPTOP-001", 2, new BigDecimal("1000.00"));
        OrderItem item2 = new OrderItem("MOUSE-001", 1, new BigDecimal("50.00"));

        Order order = new Order(Arrays.asList(item1, item2));

        assertEquals(2, order.getItems().size());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void testGetTotalAmount() {
        OrderItem item1 = new OrderItem("LAPTOP-001", 2, new BigDecimal("1000.00"));
        OrderItem item2 = new OrderItem("MOUSE-001", 3, new BigDecimal("50.00"));

        Order order = new Order(Arrays.asList(item1, item2));

        BigDecimal expectedTotal = new BigDecimal("2150.00");
        assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
    }

    @Test
    void testGetTotalAmount_EmptyItems() {
        Order order = new Order();

        assertEquals(0, BigDecimal.ZERO.compareTo(order.getTotalAmount()));
    }

    @Test
    void testOrderStatusEnum() {
        assertEquals(4, OrderStatus.values().length);
        assertNotNull(OrderStatus.valueOf("CREATED"));
        assertNotNull(OrderStatus.valueOf("PAID"));
        assertNotNull(OrderStatus.valueOf("FULFILLED"));
        assertNotNull(OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void testOrderItemGetTotalPrice() {
        OrderItem item = new OrderItem("LAPTOP-001", 3, new BigDecimal("1000.00"));

        BigDecimal expectedTotal = new BigDecimal("3000.00");
        assertEquals(0, expectedTotal.compareTo(item.getTotalPrice()));
    }

    @Test
    void testOrderGettersAndSetters() {
        Order order = new Order();

        order.setStatus(OrderStatus.PAID);
        order.setPaymentId("payment-123");
        order.setCancellationReason("Test reason");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("payment-123", order.getPaymentId());
        assertEquals("Test reason", order.getCancellationReason());
    }
}
