package com.microservices.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Embeddable
public class OrderItem {

    @Column(nullable = false)
    private String productSku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    public OrderItem() {
    }

    public OrderItem(String productSku, Integer quantity, BigDecimal price) {
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

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(quantity));
    }
}
