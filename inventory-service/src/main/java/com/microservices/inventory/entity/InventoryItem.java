package com.microservices.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String productSku;

    @Column(nullable = false)
    private Integer available;

    @Column(nullable = false)
    private Integer threshold;

    @Column(nullable = false)
    private Instant lastUpdated;

    public InventoryItem() {
        this.lastUpdated = Instant.now();
    }

    public InventoryItem(String productSku, Integer available, Integer threshold) {
        this.productSku = productSku;
        this.available = available;
        this.threshold = threshold;
        this.lastUpdated = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = Instant.now();
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
        return available != null && threshold != null && available <= threshold;
    }
}
