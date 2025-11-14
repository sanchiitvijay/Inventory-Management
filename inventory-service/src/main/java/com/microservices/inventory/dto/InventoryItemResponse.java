package com.microservices.inventory.dto;

import com.microservices.inventory.entity.InventoryItem;

import java.time.Instant;

public class InventoryItemResponse {

    private Long id;
    private String productSku;
    private Integer available;
    private Integer threshold;
    private Instant lastUpdated;
    private boolean lowStock;

    public InventoryItemResponse() {
    }

    public InventoryItemResponse(InventoryItem item) {
        this.id = item.getId();
        this.productSku = item.getProductSku();
        this.available = item.getAvailable();
        this.threshold = item.getThreshold();
        this.lastUpdated = item.getLastUpdated();
        this.lowStock = item.isLowStock();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }
}
