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
public class PaymentClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentClient.class);
    private static final String PAYMENT_SERVICE_URL = "http://payment-service";
    
    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(PAYMENT_SERVICE_URL).build();
    }

    /**
     * Process payment for an order
     * @param request Payment request
     * @return PaymentResponse or null if error occurs
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            return webClient.post()
                    .uri("/api/payments")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry exhausted for payment processing: {}", request.getOrderId());
                                return new RuntimeException("Payment service unavailable after retries");
                            }))
                    .doOnError(error -> logger.error("Error processing payment for order {}: {}", 
                            request.getOrderId(), error.getMessage()))
                    .onErrorResume(throwable -> {
                        logger.warn("Failed to process payment for order {} due to: {}", 
                                request.getOrderId(), throwable.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error processing payment for order {}: {}", 
                    request.getOrderId(), e.getMessage());
            return null;
        }
    }

    /**
     * Get payment status for an order
     * @param orderId Order ID
     * @return PaymentResponse or null if not found or error occurs
     */
    public PaymentResponse getPaymentByOrderId(String orderId) {
        try {
            return webClient.get()
                    .uri("/api/payments/order/{orderId}", orderId)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                logger.error("Retry exhausted for payment lookup: {}", orderId);
                                return new RuntimeException("Payment service unavailable after retries");
                            }))
                    .doOnError(error -> logger.error("Error fetching payment for order {}: {}", 
                            orderId, error.getMessage()))
                    .onErrorResume(throwable -> {
                        logger.warn("Returning null for payment order {} due to: {}", 
                                orderId, throwable.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error calling payment service for order {}: {}", 
                    orderId, e.getMessage());
            return null;
        }
    }
}
