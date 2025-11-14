package com.microservices.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "low_stock_alerts")
public class LowStockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer threshold;

    @Column(nullable = false)
    private Instant timestamp;

    public LowStockAlert() {
        this.timestamp = Instant.now();
    }

    public LowStockAlert(String sku, Integer availableQuantity, Integer threshold) {
        this.sku = sku;
        this.availableQuantity = availableQuantity;
        this.threshold = threshold;
        this.timestamp = Instant.now();
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
