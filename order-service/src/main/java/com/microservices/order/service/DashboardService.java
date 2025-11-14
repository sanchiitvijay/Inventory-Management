package com.microservices.order.service;

import com.microservices.order.dto.DashboardData;
import com.microservices.order.dto.LowStockItem;
import com.microservices.order.dto.OrderStatusCount;
import com.microservices.order.dto.ProductOrderCount;
import com.microservices.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private static final String INVENTORY_SERVICE_URL = "http://inventory-service";
    
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public DashboardService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClient = webClientBuilder.baseUrl(INVENTORY_SERVICE_URL).build();
    }

    public DashboardData getDashboardData() {
        DashboardData data = new DashboardData();
        
        // Get orders by status
        data.setOrdersByStatus(getOrdersByStatus());
        
        // Get top 10 products
        data.setTopProducts(getTopProducts());
        
        // Get low stock items from inventory service
        data.setLowStockItems(getLowStockItems());
        
        return data;
    }

    private List<OrderStatusCount> getOrdersByStatus() {
        try {
            List<Object[]> results = orderRepository.countOrdersByStatus();
            return results.stream()
                    .map(result -> new OrderStatusCount(
                            result[0].toString(),
                            ((Number) result[1]).longValue()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching orders by status: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ProductOrderCount> getTopProducts() {
        try {
            List<Object[]> results = orderRepository.findTopProductsByOrderCount();
            return results.stream()
                    .limit(10)
                    .map(result -> new ProductOrderCount(
                            (String) result[0],
                            ((Number) result[1]).longValue(),
                            ((Number) result[2]).longValue()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching top products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<LowStockItem> getLowStockItems() {
        try {
            return webClient.get()
                    .uri("/api/inventory/low-stock")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<LowStockItem>>() {})
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(300))
                            .doBeforeRetry(signal -> 
                                    logger.warn("Retrying low-stock request, attempt: {}", signal.totalRetries() + 1)))
                    .doOnError(error -> logger.error("Error fetching low stock items: {}", error.getMessage()))
                    .onErrorReturn(new ArrayList<>())
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error calling inventory service: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
