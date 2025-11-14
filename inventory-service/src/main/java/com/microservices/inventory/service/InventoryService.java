package com.microservices.inventory.service;

import com.microservices.inventory.dto.InventoryItemRequest;
import com.microservices.inventory.dto.InventoryItemResponse;
import com.microservices.inventory.entity.InventoryItem;
import com.microservices.inventory.event.EventLogger;
import com.microservices.inventory.event.LowStockEvent;
import com.microservices.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final EventLogger eventLogger;

    public InventoryService(InventoryRepository inventoryRepository, EventLogger eventLogger) {
        this.inventoryRepository = inventoryRepository;
        this.eventLogger = eventLogger;
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryBySku(String sku) {
        InventoryItem item = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU: " + sku));
        return new InventoryItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse createInventoryItem(InventoryItemRequest request) {
        // Check if SKU already exists
        if (inventoryRepository.findByProductSku(request.getProductSku()).isPresent()) {
            throw new RuntimeException("Inventory item with SKU " + request.getProductSku() + " already exists");
        }

        InventoryItem item = new InventoryItem(
                request.getProductSku(),
                request.getAvailable(),
                request.getThreshold()
        );

        InventoryItem savedItem = inventoryRepository.save(item);

        // Check for low stock on creation
        if (savedItem.isLowStock()) {
            logLowStockEvent(savedItem);
        }

        return new InventoryItemResponse(savedItem);
    }

    @Transactional
    public InventoryItemResponse updateInventoryItem(String sku, InventoryItemRequest request) {
        InventoryItem item = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU: " + sku));

        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }
        if (request.getThreshold() != null) {
            item.setThreshold(request.getThreshold());
        }

        InventoryItem updatedItem = inventoryRepository.save(item);

        // Check for low stock after update
        if (updatedItem.isLowStock()) {
            logLowStockEvent(updatedItem);
        }

        return new InventoryItemResponse(updatedItem);
    }

    @Transactional
    public void deductStock(String sku, int quantity) {
        InventoryItem item = inventoryRepository.findByProductSku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU: " + sku));

        if (item.getAvailable() < quantity) {
            throw new RuntimeException("Insufficient stock for SKU: " + sku + 
                    ". Available: " + item.getAvailable() + ", Requested: " + quantity);
        }

        item.setAvailable(item.getAvailable() - quantity);
        InventoryItem updatedItem = inventoryRepository.save(item);

        // Check for low stock after deduction
        if (updatedItem.isLowStock()) {
            logLowStockEvent(updatedItem);
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(InventoryItemResponse::new)
                .collect(Collectors.toList());
    }

    private void logLowStockEvent(InventoryItem item) {
        LowStockEvent event = new LowStockEvent(
                item.getProductSku(),
                item.getAvailable(),
                item.getThreshold()
        );
        eventLogger.logLowStockEvent(event);
    }

    public List<LowStockEvent> getEventLog() {
        return eventLogger.getEventLog();
    }
}
