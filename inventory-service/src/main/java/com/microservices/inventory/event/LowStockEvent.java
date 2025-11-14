package com.microservices.inventory.event;

import java.time.Instant;

public class LowStockEvent {

    private String productSku;
    private Integer availableQuantity;
    private Integer threshold;
    private Instant timestamp;

    public LowStockEvent(String productSku, Integer availableQuantity, Integer threshold) {
        this.productSku = productSku;
        this.availableQuantity = availableQuantity;
        this.threshold = threshold;
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LowStockEvent{" +
                "productSku='" + productSku + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", threshold=" + threshold +
                ", timestamp=" + timestamp +
                '}';
    }
}
