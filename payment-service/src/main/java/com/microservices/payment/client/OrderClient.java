package com.microservices.payment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OrderClient {

    private static final Logger logger = LoggerFactory.getLogger(OrderClient.class);
    private static final String ORDER_SERVICE_URL = "http://order-service";
    
    private final WebClient webClient;

    public OrderClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(ORDER_SERVICE_URL).build();
    }

    /**
     * Get order information by order ID
     * @param orderId Order ID
     * @return OrderResponse or null if not found or error occurs
     */
    public OrderResponse getOrderById(Long orderId) {
        try {
            return webClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .retrieve()
                    .bodyToMono(OrderResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry exhausted for order lookup: {}", orderId);
                                return new RuntimeException("Order service unavailable after retries");
                            }))
                    .doOnError(error -> logger.error("Error fetching order {}: {}", 
                            orderId, error.getMessage()))
                    .onErrorResume(throwable -> {
                        logger.warn("Returning null for order {} due to: {}", 
                                orderId, throwable.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error calling order service for order {}: {}", 
                    orderId, e.getMessage());
            return null;
        }
    }
}
