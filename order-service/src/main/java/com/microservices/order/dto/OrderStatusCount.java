package com.microservices.order.dto;

public class OrderStatusCount {
    private String status;
    private Long count;

    public OrderStatusCount() {
    }

    public OrderStatusCount(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
