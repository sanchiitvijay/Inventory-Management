package com.microservices.order.repository;

import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(OrderStatus status);
}
