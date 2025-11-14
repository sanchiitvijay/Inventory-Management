package com.microservices.order.dto;

public class DashboardData {
    private java.util.List<OrderStatusCount> ordersByStatus;
    private java.util.List<ProductOrderCount> topProducts;
    private java.util.List<LowStockItem> lowStockItems;

    public DashboardData() {
    }

    // Getters and Setters
    public java.util.List<OrderStatusCount> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(java.util.List<OrderStatusCount> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public java.util.List<ProductOrderCount> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(java.util.List<ProductOrderCount> topProducts) {
        this.topProducts = topProducts;
    }

    public java.util.List<LowStockItem> getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(java.util.List<LowStockItem> lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}
