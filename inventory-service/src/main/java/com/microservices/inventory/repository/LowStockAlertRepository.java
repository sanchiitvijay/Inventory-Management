package com.microservices.inventory.repository;

import com.microservices.inventory.entity.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {
    
    List<LowStockAlert> findBySku(String sku);
    
    List<LowStockAlert> findAllByOrderByTimestampDesc();
}
