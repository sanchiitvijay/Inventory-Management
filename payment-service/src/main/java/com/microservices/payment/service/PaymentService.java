package com.microservices.payment.service;

import com.microservices.payment.dto.PaymentRequest;
import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.entity.Payment;
import com.microservices.payment.entity.PaymentStatus;
import com.microservices.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process payment with deterministic logic:
     * - If amount (in cents/smallest unit) is even -> SUCCESS
     * - If amount (in cents/smallest unit) is odd -> FAILED
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Create payment entity
        Payment payment = new Payment(
                request.getOrderId(),
                request.getAmount(),
                request.getMethod()
        );

        // Simulate payment processing with deterministic logic
        PaymentStatus resultStatus = determinePaymentStatus(request.getAmount());
        payment.setStatus(resultStatus);

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);
    }

    /**
     * Deterministic payment logic:
     * - Convert amount to cents (multiply by 100)
     * - If even -> SUCCESS
     * - If odd -> FAILED
     */
    private PaymentStatus determinePaymentStatus(BigDecimal amount) {
        // Convert to cents to avoid floating point issues
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
        
        // Even amount -> SUCCESS, Odd amount -> FAILED
        return (amountInCents % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return new PaymentResponse(payment);
    }
}
