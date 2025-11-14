package com.microservices.order.service;

import com.microservices.order.client.*;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderItemRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://product-service/products";
    private static final String PAYMENT_SERVICE_URL = "http://payment-service/payments";
    private static final String INVENTORY_SERVICE_URL = "http://inventory-service/inventory";

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Validate all products exist and get their info
        List<OrderItem> orderItems = request.getItems().stream()
                .map(this::validateAndCreateOrderItem)
                .collect(Collectors.toList());

        // Create order with CREATED status
        Order order = new Order(orderItems);
        Order savedOrder = orderRepository.save(order);

        return new OrderResponse(savedOrder);
    }

    private OrderItem validateAndCreateOrderItem(OrderItemRequest itemRequest) {
        try {
            // Call product-service to validate product exists
            ProductResponse product = restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/sku/" + itemRequest.getProductSku(),
                    ProductResponse.class
            );

            if (product == null) {
                throw new RuntimeException("Product not found: " + itemRequest.getProductSku());
            }

            // Use price from product service if not provided
            BigDecimal price = itemRequest.getPrice() != null ? 
                    itemRequest.getPrice() : product.getPrice();

            return new OrderItem(itemRequest.getProductSku(), itemRequest.getQuantity(), price);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate product " + itemRequest.getProductSku() + ": " + e.getMessage());
        }
    }

    @Transactional
    public OrderResponse payOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Order cannot be paid. Current status: " + order.getStatus());
        }

        // Process payment
        PaymentRequest paymentRequest = new PaymentRequest(
                orderId,
                order.getTotalAmount(),
                "CREDIT_CARD"
        );

        try {
            PaymentResponse paymentResponse = restTemplate.postForObject(
                    PAYMENT_SERVICE_URL + "/process",
                    paymentRequest,
                    PaymentResponse.class
            );

            if (paymentResponse == null) {
                throw new RuntimeException("Payment processing failed");
            }

            order.setPaymentId(paymentResponse.getId().toString());

            // Check payment status
            if ("SUCCESS".equals(paymentResponse.getStatus())) {
                // Attempt to deduct stock
                boolean stockDeducted = deductStockForOrder(order);

                if (stockDeducted) {
                    order.setStatus(OrderStatus.PAID);
                } else {
                    // Stock deduction failed - cancel order
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setCancellationReason("Insufficient inventory to fulfill order");
                }
            } else {
                // Payment failed
                order.setStatus(OrderStatus.CREATED);
                order.setCancellationReason("Payment failed");
            }

            Order updatedOrder = orderRepository.save(order);
            return new OrderResponse(updatedOrder);

        } catch (Exception e) {
            throw new RuntimeException("Payment processing error: " + e.getMessage());
        }
    }

    private boolean deductStockForOrder(Order order) {
        try {
            for (OrderItem item : order.getItems()) {
                // Get current inventory
                InventoryResponse inventory = restTemplate.getForObject(
                        INVENTORY_SERVICE_URL + "/" + item.getProductSku(),
                        InventoryResponse.class
                );

                if (inventory == null) {
                    throw new RuntimeException("Inventory not found for SKU: " + item.getProductSku());
                }

                // Check if sufficient stock available
                if (inventory.getAvailable() < item.getQuantity()) {
                    return false; // Insufficient stock
                }

                // Deduct stock
                int newQuantity = inventory.getAvailable() - item.getQuantity();
                InventoryUpdateRequest updateRequest = new InventoryUpdateRequest(newQuantity);

                restTemplate.put(
                        INVENTORY_SERVICE_URL + "/" + item.getProductSku(),
                        updateRequest
                );
            }
            return true;
        } catch (Exception e) {
            // If any stock deduction fails, return false
            return false;
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }
}
