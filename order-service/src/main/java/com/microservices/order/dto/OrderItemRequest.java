package com.microservices.order.dto;

import java.math.BigDecimal;

public class OrderItemRequest {

    private String productSku;
    private Integer quantity;
    private BigDecimal price;

    public OrderItemRequest() {
    }

    public OrderItemRequest(String productSku, Integer quantity, BigDecimal price) {
        this.productSku = productSku;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
