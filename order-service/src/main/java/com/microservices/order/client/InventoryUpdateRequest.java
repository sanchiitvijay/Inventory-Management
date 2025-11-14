package com.microservices.order.client;

import java.math.BigDecimal;

public class InventoryUpdateRequest {

    private Integer available;
    private Integer threshold;

    public InventoryUpdateRequest() {
    }

    public InventoryUpdateRequest(Integer available) {
        this.available = available;
    }

    // Getters and Setters
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
