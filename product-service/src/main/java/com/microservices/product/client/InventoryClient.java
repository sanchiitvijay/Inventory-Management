package com.microservices.product.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class InventoryClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryClient.class);
    private static final String INVENTORY_SERVICE_URL = "http://inventory-service";
    
    private final WebClient webClient;

    public InventoryClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(INVENTORY_SERVICE_URL).build();
    }

    /**
     * Get inventory information for a product by SKU
     * @param sku Product SKU
     * @return InventoryResponse or null if not found or error occurs
     */
    public InventoryResponse getInventoryBySku(String sku) {
        try {
            return webClient.get()
                    .uri("/api/inventory/sku/{sku}", sku)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry exhausted for inventory lookup: {}", sku);
                                return new RuntimeException("Inventory service unavailable after retries");
                            }))
                    .doOnError(error -> logger.error("Error fetching inventory for SKU {}: {}", 
                            sku, error.getMessage()))
                    .onErrorResume(throwable -> {
                        logger.warn("Returning null for inventory SKU {} due to: {}", 
                                sku, throwable.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error calling inventory service for SKU {}: {}", 
                    sku, e.getMessage());
            return null;
        }
    }
}
