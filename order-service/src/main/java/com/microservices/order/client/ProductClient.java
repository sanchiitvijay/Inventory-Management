package com.microservices.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);
    private static final String PRODUCT_SERVICE_URL = "http://product-service";
    
    private final WebClient webClient;

    public ProductClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(PRODUCT_SERVICE_URL).build();
    }

    /**
     * Get product information by ID
     * @param productId Product ID
     * @return ProductResponse or null if not found or error occurs
     */
    public ProductResponse getProductById(Long productId) {
        try {
            return webClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry exhausted for product lookup: {}", productId);
                                return new RuntimeException("Product service unavailable after retries");
                            }))
                    .doOnError(error -> logger.error("Error fetching product {}: {}", 
                            productId, error.getMessage()))
                    .onErrorResume(throwable -> {
                        logger.warn("Returning null for product {} due to: {}", 
                                productId, throwable.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error calling product service for product {}: {}", 
                    productId, e.getMessage());
            return null;
        }
    }
}
