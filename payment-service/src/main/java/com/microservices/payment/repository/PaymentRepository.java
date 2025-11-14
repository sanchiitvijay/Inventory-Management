package com.microservices.payment.repository;

import com.microservices.payment.entity.Payment;
import com.microservices.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByStatus(PaymentStatus status);
}
