package com.microservices.order.dto;

public class ProductOrderCount {
    private String productSku;
    private Long orderCount;
    private Long totalQuantity;

    public ProductOrderCount() {
    }

    public ProductOrderCount(String productSku, Long orderCount, Long totalQuantity) {
        this.productSku = productSku;
        this.orderCount = orderCount;
        this.totalQuantity = totalQuantity;
    }

    // Getters and Setters
    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
