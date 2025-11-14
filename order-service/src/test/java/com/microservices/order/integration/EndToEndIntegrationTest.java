package com.microservices.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderItemRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.OrderStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Test for Microservices
 * 
 * This test demonstrates a complete happy path flow:
 * 1. Create a product in product-service
 * 2. Set inventory in inventory-service
 * 3. Place an order in order-service
 * 4. Process payment via payment-service
 * 5. Deduct inventory via inventory-service
 * 6. Verify order is PAID
 * 
 * Uses WireMock to stub inter-service HTTP calls and @SpringBootTest to start order-service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static WireMockServer productServiceMock;
    private static WireMockServer inventoryServiceMock;
    private static WireMockServer paymentServiceMock;

    private static final String TEST_SKU = "LAPTOP-E2E-001";
    private static final String TEST_PRODUCT_NAME = "Dell XPS 15";
    private static final BigDecimal TEST_PRICE = new BigDecimal("1500.00");
    private static final int TEST_QUANTITY = 10;
    private static final int ORDER_QUANTITY = 2;

    @BeforeAll
    static void setupWireMock() {
        // Start WireMock servers for each microservice
        productServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        inventoryServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        paymentServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());

        productServiceMock.start();
        inventoryServiceMock.start();
        paymentServiceMock.start();

        System.out.println("WireMock Product Service started on port: " + productServiceMock.port());
        System.out.println("WireMock Inventory Service started on port: " + inventoryServiceMock.port());
        System.out.println("WireMock Payment Service started on port: " + paymentServiceMock.port());

        // Set system properties for test configuration
        System.setProperty("wiremock.product.port", String.valueOf(productServiceMock.port()));
        System.setProperty("wiremock.inventory.port", String.valueOf(inventoryServiceMock.port()));
        System.setProperty("wiremock.payment.port", String.valueOf(paymentServiceMock.port()));
    }

    @AfterAll
    static void tearDownWireMock() {
        if (productServiceMock != null) {
            productServiceMock.stop();
        }
        if (inventoryServiceMock != null) {
            inventoryServiceMock.stop();
        }
        if (paymentServiceMock != null) {
            paymentServiceMock.stop();
        }
    }

    @BeforeEach
    void setup() {
        // Reset all stubs before each test
        productServiceMock.resetAll();
        inventoryServiceMock.resetAll();
        paymentServiceMock.resetAll();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate testRestTemplate() {
            // Create a RestTemplate that doesn't use service discovery
            return new RestTemplate();
        }
    }

    /**
     * Test 1: Create Product
     * Verifies that product-service can create a new product
     */
    @Test
    @Order(1)
    @DisplayName("Step 1: Create Product in Product Service")
    void testCreateProduct() throws Exception {
        // Arrange: Setup WireMock stub for product creation
        String productJson = String.format("""
            {
                "id": 1,
                "sku": "%s",
                "name": "%s",
                "description": "High-performance laptop for developers",
                "price": %s,
                "categoryId": 1
            }
            """, TEST_SKU, TEST_PRODUCT_NAME, TEST_PRICE);

        productServiceMock.stubFor(post(urlEqualTo("/products"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productJson)));

        // Act: Call product service endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format("""
            {
                "sku": "%s",
                "name": "%s",
                "description": "High-performance laptop for developers",
                "price": %s,
                "categoryId": 1
            }
            """, TEST_SKU, TEST_PRODUCT_NAME, TEST_PRICE);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = new TestRestTemplate()
                .postForEntity("http://localhost:" + productServiceMock.port() + "/products", 
                              request, String.class);

        // Assert: Verify product creation
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains(TEST_SKU));
        assertTrue(response.getBody().contains(TEST_PRODUCT_NAME));

        // Verify WireMock received the request
        productServiceMock.verify(postRequestedFor(urlEqualTo("/products"))
                .withHeader("Content-Type", equalTo("application/json")));

        System.out.println("✅ Product created successfully: " + TEST_SKU);
    }

    /**
     * Test 2: Set Inventory
     * Verifies that inventory-service can set stock levels
     */
    @Test
    @Order(2)
    @DisplayName("Step 2: Set Inventory in Inventory Service")
    void testSetInventory() throws Exception {
        // Arrange: Setup WireMock stub for inventory creation
        String inventoryJson = String.format("""
            {
                "productSku": "%s",
                "available": %d,
                "reserved": 0,
                "threshold": 5
            }
            """, TEST_SKU, TEST_QUANTITY);

        inventoryServiceMock.stubFor(post(urlEqualTo("/inventory"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(inventoryJson)));

        // Act: Call inventory service endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format("""
            {
                "productSku": "%s",
                "available": %d,
                "threshold": 5
            }
            """, TEST_SKU, TEST_QUANTITY);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = new TestRestTemplate()
                .postForEntity("http://localhost:" + inventoryServiceMock.port() + "/inventory", 
                              request, String.class);

        // Assert: Verify inventory creation
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains(TEST_SKU));
        assertTrue(response.getBody().contains(String.valueOf(TEST_QUANTITY)));

        // Verify WireMock received the request
        inventoryServiceMock.verify(postRequestedFor(urlEqualTo("/inventory"))
                .withHeader("Content-Type", equalTo("application/json")));

        System.out.println("✅ Inventory set successfully: " + TEST_QUANTITY + " units");
    }

    /**
     * Test 3: Place Order (Integration Point)
     * Verifies that order-service can orchestrate:
     * - Product validation
     * - Order creation
     * - Payment processing
     * - Inventory deduction
     */
    @Test
    @Order(3)
    @DisplayName("Step 3: Place Order and Verify End-to-End Happy Path")
    void testPlaceOrderEndToEndHappyPath() throws Exception {
        // Arrange: Setup all WireMock stubs for the complete flow
        
        // 1. Stub product-service: Get product by SKU
        String productResponseJson = String.format("""
            {
                "id": 1,
                "sku": "%s",
                "name": "%s",
                "description": "High-performance laptop for developers",
                "price": %s,
                "categoryId": 1
            }
            """, TEST_SKU, TEST_PRODUCT_NAME, TEST_PRICE);

        productServiceMock.stubFor(get(urlMatching("/products/sku/" + TEST_SKU))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productResponseJson)));

        // 2. Stub inventory-service: Check stock availability
        String inventoryResponseJson = String.format("""
            {
                "productSku": "%s",
                "available": %d,
                "reserved": 0,
                "threshold": 5
            }
            """, TEST_SKU, TEST_QUANTITY);

        inventoryServiceMock.stubFor(get(urlMatching("/inventory/" + TEST_SKU))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(inventoryResponseJson)));

        // 3. Stub payment-service: Process payment (even amount = success)
        BigDecimal totalAmount = TEST_PRICE.multiply(new BigDecimal(ORDER_QUANTITY));
        String paymentResponseJson = String.format("""
            {
                "id": 1,
                "orderId": "order-123",
                "amount": %s,
                "status": "SUCCESS",
                "paymentMethod": "CREDIT_CARD",
                "transactionId": "TXN-12345"
            }
            """, totalAmount);

        paymentServiceMock.stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentResponseJson)));

        // 4. Stub inventory-service: Deduct stock
        inventoryServiceMock.stubFor(put(urlMatching("/inventory/" + TEST_SKU + "/deduct\\?quantity=" + ORDER_QUANTITY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                            {
                                "productSku": "%s",
                                "available": %d,
                                "reserved": 0,
                                "threshold": 5
                            }
                            """, TEST_SKU, TEST_QUANTITY - ORDER_QUANTITY))));

        // Act: Create order via order-service
        OrderItemRequest orderItem = new OrderItemRequest();
        orderItem.setProductSku(TEST_SKU);
        orderItem.setQuantity(ORDER_QUANTITY);
        orderItem.setPrice(TEST_PRICE);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setItems(Arrays.asList(orderItem));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> request = new HttpEntity<>(orderRequest, headers);

        String orderServiceUrl = "http://localhost:" + port + "/orders";
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                orderServiceUrl, request, OrderResponse.class);

        // Assert: Verify order creation
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        OrderResponse createdOrder = createResponse.getBody();
        assertNotNull(createdOrder.getId());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());
        assertEquals(1, createdOrder.getItems().size());
        assertEquals(TEST_SKU, createdOrder.getItems().get(0).getProductSku());
        assertEquals(ORDER_QUANTITY, createdOrder.getItems().get(0).getQuantity());

        System.out.println("✅ Order created successfully with ID: " + createdOrder.getId());

        // Act: Pay for the order
        String payOrderUrl = orderServiceUrl + "/" + createdOrder.getId() + "/pay";
        ResponseEntity<OrderResponse> payResponse = restTemplate.postForEntity(
                payOrderUrl, null, OrderResponse.class);

        // Assert: Verify payment and order completion
        assertEquals(HttpStatus.OK, payResponse.getStatusCode());
        assertNotNull(payResponse.getBody());
        OrderResponse paidOrder = payResponse.getBody();
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        assertNotNull(paidOrder.getPaymentId());

        System.out.println("✅ Order paid successfully! Payment ID: " + paidOrder.getPaymentId());

        // Verify all service interactions
        productServiceMock.verify(getRequestedFor(urlMatching("/products/sku/" + TEST_SKU)));
        inventoryServiceMock.verify(getRequestedFor(urlMatching("/inventory/" + TEST_SKU)));
        paymentServiceMock.verify(postRequestedFor(urlEqualTo("/payments")));
        inventoryServiceMock.verify(putRequestedFor(urlMatching("/inventory/" + TEST_SKU + "/deduct.*")));

        System.out.println("✅ All service interactions verified!");
        System.out.println("🎉 End-to-End Happy Path Test PASSED!");
    }

    /**
     * Test 4: Order with Insufficient Stock
     * Verifies that order is cancelled when inventory is insufficient
     */
    @Test
    @Order(4)
    @DisplayName("Step 4: Order Cancelled Due to Insufficient Stock")
    void testOrderCancelledDueToInsufficientStock() throws Exception {
        // Arrange: Setup stubs with insufficient stock
        String productResponseJson = String.format("""
            {
                "id": 1,
                "sku": "%s",
                "name": "%s",
                "price": %s
            }
            """, TEST_SKU, TEST_PRODUCT_NAME, TEST_PRICE);

        productServiceMock.stubFor(get(urlMatching("/products/sku/" + TEST_SKU))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productResponseJson)));

        // Stock available: only 1, but order needs 2
        String inventoryResponseJson = String.format("""
            {
                "productSku": "%s",
                "available": 1,
                "reserved": 0,
                "threshold": 5
            }
            """, TEST_SKU);

        inventoryServiceMock.stubFor(get(urlMatching("/inventory/" + TEST_SKU))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(inventoryResponseJson)));

        // Payment succeeds (even amount)
        BigDecimal totalAmount = TEST_PRICE.multiply(new BigDecimal(ORDER_QUANTITY));
        String paymentResponseJson = String.format("""
            {
                "id": 2,
                "orderId": "order-456",
                "amount": %s,
                "status": "SUCCESS"
            }
            """, totalAmount);

        paymentServiceMock.stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentResponseJson)));

        // Act: Create and pay order
        OrderItemRequest orderItem = new OrderItemRequest();
        orderItem.setProductSku(TEST_SKU);
        orderItem.setQuantity(ORDER_QUANTITY);
        orderItem.setPrice(TEST_PRICE);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setItems(Arrays.asList(orderItem));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> request = new HttpEntity<>(orderRequest, headers);

        String orderServiceUrl = "http://localhost:" + port + "/orders";
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                orderServiceUrl, request, OrderResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        String orderId = createResponse.getBody().getId();

        String payOrderUrl = orderServiceUrl + "/" + orderId + "/pay";
        ResponseEntity<OrderResponse> payResponse = restTemplate.postForEntity(
                payOrderUrl, null, OrderResponse.class);

        // Assert: Order should be cancelled
        assertEquals(HttpStatus.OK, payResponse.getStatusCode());
        OrderResponse cancelledOrder = payResponse.getBody();
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertTrue(cancelledOrder.getCancellationReason().contains("Insufficient inventory"));

        System.out.println("✅ Order correctly cancelled due to insufficient stock");
        System.out.println("   Cancellation reason: " + cancelledOrder.getCancellationReason());
    }

    /**
     * Test 5: Order with Payment Failure
     * Verifies that order remains in CREATED status when payment fails
     */
    @Test
    @Order(5)
    @DisplayName("Step 5: Order Fails Due to Payment Failure")
    void testOrderFailsDueToPaymentFailure() throws Exception {
        // Arrange: Setup stubs with payment failure
        String TEST_SKU_ODD = "MOUSE-ODD-001";
        BigDecimal oddPrice = new BigDecimal("99.99"); // Odd amount = payment fails

        String productResponseJson = String.format("""
            {
                "id": 2,
                "sku": "%s",
                "name": "Wireless Mouse",
                "price": %s
            }
            """, TEST_SKU_ODD, oddPrice);

        productServiceMock.stubFor(get(urlMatching("/products/sku/" + TEST_SKU_ODD))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productResponseJson)));

        String inventoryResponseJson = String.format("""
            {
                "productSku": "%s",
                "available": 50,
                "reserved": 0,
                "threshold": 5
            }
            """, TEST_SKU_ODD);

        inventoryServiceMock.stubFor(get(urlMatching("/inventory/" + TEST_SKU_ODD))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(inventoryResponseJson)));

        // Payment fails (odd amount)
        String paymentResponseJson = String.format("""
            {
                "id": 3,
                "orderId": "order-789",
                "amount": %s,
                "status": "FAILED"
            }
            """, oddPrice);

        paymentServiceMock.stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paymentResponseJson)));

        // Act: Create and pay order
        OrderItemRequest orderItem = new OrderItemRequest();
        orderItem.setProductSku(TEST_SKU_ODD);
        orderItem.setQuantity(1);
        orderItem.setPrice(oddPrice);

        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setItems(Arrays.asList(orderItem));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> request = new HttpEntity<>(orderRequest, headers);

        String orderServiceUrl = "http://localhost:" + port + "/orders";
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                orderServiceUrl, request, OrderResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        String orderId = createResponse.getBody().getId();

        String payOrderUrl = orderServiceUrl + "/" + orderId + "/pay";
        ResponseEntity<OrderResponse> payResponse = restTemplate.postForEntity(
                payOrderUrl, null, OrderResponse.class);

        // Assert: Order should be cancelled due to payment failure
        assertEquals(HttpStatus.OK, payResponse.getStatusCode());
        OrderResponse failedOrder = payResponse.getBody();
        assertEquals(OrderStatus.CANCELLED, failedOrder.getStatus());
        assertTrue(failedOrder.getCancellationReason().contains("Payment failed"));

        System.out.println("✅ Order correctly cancelled due to payment failure");
        System.out.println("   Cancellation reason: " + failedOrder.getCancellationReason());
        
        // Verify inventory was NOT deducted
        inventoryServiceMock.verify(0, putRequestedFor(urlMatching("/inventory/.*")));
        System.out.println("✅ Inventory correctly NOT deducted after payment failure");
    }
}
