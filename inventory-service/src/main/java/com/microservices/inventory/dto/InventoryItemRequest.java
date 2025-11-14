package com.microservices.inventory.dto;

public class InventoryItemRequest {

    private String productSku;
    private Integer available;
    private Integer threshold;

    public InventoryItemRequest() {
    }

    public InventoryItemRequest(String productSku, Integer available, Integer threshold) {
        this.productSku = productSku;
        this.available = available;
        this.threshold = threshold;
    }

    // Getters and Setters
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
}
