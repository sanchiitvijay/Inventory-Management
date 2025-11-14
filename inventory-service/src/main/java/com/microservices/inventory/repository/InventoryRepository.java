package com.microservices.inventory.repository;

import com.microservices.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductSku(String productSku);

    @Query("SELECT i FROM InventoryItem i WHERE i.available <= i.threshold")
    List<InventoryItem> findLowStockItems();
}
