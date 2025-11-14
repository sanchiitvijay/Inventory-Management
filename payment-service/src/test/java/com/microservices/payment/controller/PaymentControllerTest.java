package com.microservices.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.payment.dto.PaymentRequest;
import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.entity.PaymentStatus;
import com.microservices.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void testProcessPayment_EvenAmount_Success() throws Exception {
        // Arrange
        PaymentRequest request = new PaymentRequest("ORDER-001", new BigDecimal("100.00"), "CREDIT_CARD");

        PaymentResponse response = new PaymentResponse();
        response.setId(1L);
        response.setOrderId("ORDER-001");
        response.setAmount(new BigDecimal("100.00"));
        response.setStatus(PaymentStatus.SUCCESS);
        response.setMethod("CREDIT_CARD");
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"));
    }

    @Test
    void testProcessPayment_OddAmount_Failed() throws Exception {
        // Arrange
        PaymentRequest request = new PaymentRequest("ORDER-002", new BigDecimal("99.99"), "DEBIT_CARD");

        PaymentResponse response = new PaymentResponse();
        response.setId(2L);
        response.setOrderId("ORDER-002");
        response.setAmount(new BigDecimal("99.99"));
        response.setStatus(PaymentStatus.FAILED);
        response.setMethod("DEBIT_CARD");
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORDER-002"))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.method").value("DEBIT_CARD"));
    }

    @Test
    void testProcessPayment_BadRequest() throws Exception {
        // Arrange
        PaymentRequest request = new PaymentRequest("ORDER-003", new BigDecimal("50.00"), "CARD");

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Processing error"));

        // Act & Assert
        mockMvc.perform(post("/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPayment_Success() throws Exception {
        // Arrange
        PaymentResponse response = new PaymentResponse();
        response.setId(1L);
        response.setOrderId("ORDER-001");
        response.setAmount(new BigDecimal("100.00"));
        response.setStatus(PaymentStatus.SUCCESS);
        response.setMethod("CREDIT_CARD");
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(paymentService.getPaymentById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGetPayment_NotFound() throws Exception {
        // Arrange
        when(paymentService.getPaymentById(999L))
                .thenThrow(new RuntimeException("Payment not found"));

        // Act & Assert
        mockMvc.perform(get("/payments/999"))
                .andExpect(status().isNotFound());
    }
}
