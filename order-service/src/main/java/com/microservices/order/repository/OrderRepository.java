package com.microservices.order.repository;

import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o.status as status, COUNT(o) as count FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT item.productSku as productSku, COUNT(DISTINCT o.id) as orderCount, SUM(item.quantity) as totalQuantity " +
           "FROM Order o JOIN o.items item " +
           "GROUP BY item.productSku " +
           "ORDER BY COUNT(DISTINCT o.id) DESC")
    List<Object[]> findTopProductsByOrderCount();
}
