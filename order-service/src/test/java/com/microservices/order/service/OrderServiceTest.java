package com.microservices.order.service;

import com.microservices.order.client.*;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderItemRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    private ProductResponse testProduct;
    private InventoryResponse testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new ProductResponse();
        testProduct.setId(1L);
        testProduct.setSku("LAPTOP-001");
        testProduct.setName("Test Laptop");
        testProduct.setPrice(new BigDecimal("1000.00"));

        testInventory = new InventoryResponse();
        testInventory.setId(1L);
        testInventory.setProductSku("LAPTOP-001");
        testInventory.setAvailable(100);
        testInventory.setThreshold(10);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest("LAPTOP-001", 2, new BigDecimal("1000.00"));
        CreateOrderRequest request = new CreateOrderRequest(Arrays.asList(itemRequest));

        when(restTemplate.getForObject(anyString(), eq(ProductResponse.class)))
                .thenReturn(testProduct);

        Order savedOrder = new Order();
        savedOrder.setId("test-order-id");
        savedOrder.getItems().add(new OrderItem("LAPTOP-001", 2, new BigDecimal("1000.00")));
        savedOrder.setStatus(OrderStatus.CREATED);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("test-order-id", response.getId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(1, response.getItems().size());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductResponse.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest("INVALID-SKU", 1, new BigDecimal("100.00"));
        CreateOrderRequest request = new CreateOrderRequest(Arrays.asList(itemRequest));

        when(restTemplate.getForObject(anyString(), eq(ProductResponse.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(request);
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPayOrder_HappyPath_PaymentSuccessAndStockDeducted() {
        // Arrange
        Order order = new Order();
        order.setId("order-123");
        order.getItems().add(new OrderItem("LAPTOP-001", 2, new BigDecimal("1000.00")));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(1L);
        paymentResponse.setStatus("SUCCESS");

        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
                .thenReturn(paymentResponse);

        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(testInventory);

        doNothing().when(restTemplate).put(anyString(), any());

        Order paidOrder = new Order();
        paidOrder.setId("order-123");
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setPaymentId("1");

        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

        // Act
        OrderResponse response = orderService.payOrder("order-123");

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PAID, response.getStatus());
        assertEquals("1", response.getPaymentId());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponse.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(InventoryResponse.class));
        verify(restTemplate, times(1)).put(anyString(), any());
    }

    @Test
    void testPayOrder_InsufficientStock_OrderCancelled() {
        // Arrange
        Order order = new Order();
        order.setId("order-456");
        order.getItems().add(new OrderItem("LAPTOP-001", 200, new BigDecimal("1000.00")));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("order-456")).thenReturn(Optional.of(order));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(2L);
        paymentResponse.setStatus("SUCCESS");

        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
                .thenReturn(paymentResponse);

        // Inventory only has 100 available, but order needs 200
        when(restTemplate.getForObject(anyString(), eq(InventoryResponse.class)))
                .thenReturn(testInventory);

        Order cancelledOrder = new Order();
        cancelledOrder.setId("order-456");
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        cancelledOrder.setPaymentId("2");
        cancelledOrder.setCancellationReason("Insufficient inventory to fulfill order");

        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        // Act
        OrderResponse response = orderService.payOrder("order-456");

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals("Insufficient inventory to fulfill order", response.getCancellationReason());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponse.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(InventoryResponse.class));
        verify(restTemplate, never()).put(anyString(), any()); // Stock not deducted
    }

    @Test
    void testPayOrder_PaymentFailed() {
        // Arrange
        Order order = new Order();
        order.setId("order-789");
        order.getItems().add(new OrderItem("LAPTOP-001", 1, new BigDecimal("999.99")));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("order-789")).thenReturn(Optional.of(order));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(3L);
        paymentResponse.setStatus("FAILED");

        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
                .thenReturn(paymentResponse);

        Order failedOrder = new Order();
        failedOrder.setId("order-789");
        failedOrder.setStatus(OrderStatus.CREATED);
        failedOrder.setPaymentId("3");
        failedOrder.setCancellationReason("Payment failed");

        when(orderRepository.save(any(Order.class))).thenReturn(failedOrder);

        // Act
        OrderResponse response = orderService.payOrder("order-789");

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals("Payment failed", response.getCancellationReason());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponse.class));
        verify(restTemplate, never()).getForObject(anyString(), eq(InventoryResponse.class));
    }

    @Test
    void testPayOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.payOrder("non-existent");
        });
    }

    @Test
    void testPayOrder_OrderAlreadyPaid() {
        // Arrange
        Order order = new Order();
        order.setId("order-paid");
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById("order-paid")).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.payOrder("order-paid");
        });
    }

    @Test
    void testGetOrder_Success() {
        // Arrange
        Order order = new Order();
        order.setId("order-123");
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrder("order-123");

        // Assert
        assertNotNull(response);
        assertEquals("order-123", response.getId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
    }

    @Test
    void testGetOrder_NotFound() {
        // Arrange
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrder("non-existent");
        });
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        Order order1 = new Order();
        order1.setId("order-1");
        Order order2 = new Order();
        order2.setId("order-2");

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testPayOrder_MultipleItems_AllStockAvailable() {
        // Arrange
        Order order = new Order();
        order.setId("order-multi");
        order.getItems().add(new OrderItem("LAPTOP-001", 2, new BigDecimal("1000.00")));
        order.getItems().add(new OrderItem("MOUSE-001", 5, new BigDecimal("50.00")));
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("order-multi")).thenReturn(Optional.of(order));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(4L);
        paymentResponse.setStatus("SUCCESS");

        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
                .thenReturn(paymentResponse);

        InventoryResponse inventory1 = new InventoryResponse();
        inventory1.setProductSku("LAPTOP-001");
        inventory1.setAvailable(100);

        InventoryResponse inventory2 = new InventoryResponse();
        inventory2.setProductSku("MOUSE-001");
        inventory2.setAvailable(50);

        when(restTemplate.getForObject(contains("LAPTOP-001"), eq(InventoryResponse.class)))
                .thenReturn(inventory1);
        when(restTemplate.getForObject(contains("MOUSE-001"), eq(InventoryResponse.class)))
                .thenReturn(inventory2);

        doNothing().when(restTemplate).put(anyString(), any());

        Order paidOrder = new Order();
        paidOrder.setId("order-multi");
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setPaymentId("4");

        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

        // Act
        OrderResponse response = orderService.payOrder("order-multi");

        // Assert
        assertEquals(OrderStatus.PAID, response.getStatus());
        verify(restTemplate, times(2)).getForObject(anyString(), eq(InventoryResponse.class));
        verify(restTemplate, times(2)).put(anyString(), any());
    }
}
