package com.microservices.inventory.dto;

import com.microservices.inventory.entity.LowStockAlert;

import java.time.Instant;

public class LowStockAlertResponse {

    private Long id;
    private String sku;
    private Integer availableQuantity;
    private Integer threshold;
    private Instant timestamp;

    public LowStockAlertResponse() {
    }

    public LowStockAlertResponse(LowStockAlert alert) {
        this.id = alert.getId();
        this.sku = alert.getSku();
        this.availableQuantity = alert.getAvailableQuantity();
        this.threshold = alert.getThreshold();
        this.timestamp = alert.getTimestamp();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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
}
