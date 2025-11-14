package com.microservices.payment.service;

import com.microservices.payment.dto.PaymentRequest;
import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.entity.Payment;
import com.microservices.payment.entity.PaymentStatus;
import com.microservices.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        // Mock repository will be initialized by Mockito
    }

    @Test
    void testProcessPayment_EvenAmount_Success() {
        // Arrange - Even amount (100.00 -> 10000 cents)
        PaymentRequest request = new PaymentRequest("ORDER-001", new BigDecimal("100.00"), "CREDIT_CARD");
        
        Payment savedPayment = new Payment("ORDER-001", new BigDecimal("100.00"), "CREDIT_CARD");
        savedPayment.setId(1L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("ORDER-001", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("CREDIT_CARD", response.getMethod());
        
        // Verify repository interaction
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
    }

    @Test
    void testProcessPayment_OddAmount_Failed() {
        // Arrange - Odd amount (99.99 -> 9999 cents)
        PaymentRequest request = new PaymentRequest("ORDER-002", new BigDecimal("99.99"), "DEBIT_CARD");
        
        Payment savedPayment = new Payment("ORDER-002", new BigDecimal("99.99"), "DEBIT_CARD");
        savedPayment.setId(2L);
        savedPayment.setStatus(PaymentStatus.FAILED);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals("ORDER-002", response.getOrderId());
        assertEquals(new BigDecimal("99.99"), response.getAmount());
        assertEquals("DEBIT_CARD", response.getMethod());
        
        // Verify repository interaction
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.FAILED, paymentCaptor.getValue().getStatus());
    }

    @Test
    void testProcessPayment_SmallEvenAmount_Success() {
        // Arrange - Small even amount (10.50 -> 1050 cents)
        PaymentRequest request = new PaymentRequest("ORDER-003", new BigDecimal("10.50"), "PAYPAL");
        
        Payment savedPayment = new Payment("ORDER-003", new BigDecimal("10.50"), "PAYPAL");
        savedPayment.setId(3L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_SmallOddAmount_Failed() {
        // Arrange - Small odd amount (5.25 -> 525 cents)
        PaymentRequest request = new PaymentRequest("ORDER-004", new BigDecimal("5.25"), "WALLET");
        
        Payment savedPayment = new Payment("ORDER-004", new BigDecimal("5.25"), "WALLET");
        savedPayment.setId(4L);
        savedPayment.setStatus(PaymentStatus.FAILED);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_ZeroAmount_Success() {
        // Arrange - Zero is even (0.00 -> 0 cents)
        PaymentRequest request = new PaymentRequest("ORDER-005", new BigDecimal("0.00"), "FREE");
        
        Payment savedPayment = new Payment("ORDER-005", new BigDecimal("0.00"), "FREE");
        savedPayment.setId(5L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
    }

    @Test
    void testProcessPayment_LargeEvenAmount_Success() {
        // Arrange - Large even amount (1000000.00 -> 100000000 cents)
        PaymentRequest request = new PaymentRequest("ORDER-006", new BigDecimal("1000000.00"), "WIRE_TRANSFER");
        
        Payment savedPayment = new Payment("ORDER-006", new BigDecimal("1000000.00"), "WIRE_TRANSFER");
        savedPayment.setId(6L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("1000000.00"), response.getAmount());
    }

    @Test
    void testProcessPayment_OnecentOdd_Failed() {
        // Arrange - 0.01 -> 1 cent (odd)
        PaymentRequest request = new PaymentRequest("ORDER-007", new BigDecimal("0.01"), "CREDIT_CARD");
        
        Payment savedPayment = new Payment("ORDER-007", new BigDecimal("0.01"), "CREDIT_CARD");
        savedPayment.setId(7L);
        savedPayment.setStatus(PaymentStatus.FAILED);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.FAILED, response.getStatus());
    }

    @Test
    void testProcessPayment_TwocentEven_Success() {
        // Arrange - 0.02 -> 2 cents (even)
        PaymentRequest request = new PaymentRequest("ORDER-008", new BigDecimal("0.02"), "CREDIT_CARD");
        
        Payment savedPayment = new Payment("ORDER-008", new BigDecimal("0.02"), "CREDIT_CARD");
        savedPayment.setId(8L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
    }

    @Test
    void testGetPaymentById_Success() {
        // Arrange
        Payment payment = new Payment("ORDER-001", new BigDecimal("100.00"), "CREDIT_CARD");
        payment.setId(1L);
        payment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPaymentById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORDER-001", response.getOrderId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentById(999L);
        });
        verify(paymentRepository, times(1)).findById(999L);
    }

    @Test
    void testPaymentRequestResponseMapping() {
        // Arrange
        PaymentRequest request = new PaymentRequest("ORDER-100", new BigDecimal("250.00"), "CREDIT_CARD");
        
        Payment savedPayment = new Payment("ORDER-100", new BigDecimal("250.00"), "CREDIT_CARD");
        savedPayment.setId(100L);
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(request.getMethod(), response.getMethod());
        assertNotNull(response.getId());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }
}
