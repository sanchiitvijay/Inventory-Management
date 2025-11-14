package com.microservices.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderItemRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void testCreateOrder_Success() throws Exception {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest("LAPTOP-001", 2, new BigDecimal("1000.00"));
        CreateOrderRequest request = new CreateOrderRequest(Arrays.asList(itemRequest));

        OrderResponse response = new OrderResponse();
        response.setId("order-123");
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("2000.00"));
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order-123"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(2000.00));
    }

    @Test
    void testCreateOrder_BadRequest() throws Exception {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest("INVALID-SKU", 1, new BigDecimal("100.00"));
        CreateOrderRequest request = new CreateOrderRequest(Arrays.asList(itemRequest));

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPayOrder_Success() throws Exception {
        // Arrange
        OrderResponse response = new OrderResponse();
        response.setId("order-123");
        response.setStatus(OrderStatus.PAID);
        response.setPaymentId("payment-456");
        response.setTotalAmount(new BigDecimal("2000.00"));
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(orderService.payOrder(eq("order-123"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/orders/order-123/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-123"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentId").value("payment-456"));
    }

    @Test
    void testPayOrder_BadRequest() throws Exception {
        // Arrange
        when(orderService.payOrder(eq("order-invalid")))
                .thenThrow(new RuntimeException("Order not found"));

        // Act & Assert
        mockMvc.perform(post("/orders/order-invalid/pay"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrder_Success() throws Exception {
        // Arrange
        OrderResponse response = new OrderResponse();
        response.setId("order-123");
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("1000.00"));

        when(orderService.getOrder(eq("order-123"))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/orders/order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-123"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void testGetOrder_NotFound() throws Exception {
        // Arrange
        when(orderService.getOrder(eq("non-existent")))
                .thenThrow(new RuntimeException("Order not found"));

        // Act & Assert
        mockMvc.perform(get("/orders/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllOrders() throws Exception {
        // Arrange
        OrderResponse order1 = new OrderResponse();
        order1.setId("order-1");
        order1.setStatus(OrderStatus.CREATED);

        OrderResponse order2 = new OrderResponse();
        order2.setId("order-2");
        order2.setStatus(OrderStatus.PAID);

        List<OrderResponse> orders = Arrays.asList(order1, order2);

        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("order-1"))
                .andExpect(jsonPath("$[1].id").value("order-2"));
    }
}
