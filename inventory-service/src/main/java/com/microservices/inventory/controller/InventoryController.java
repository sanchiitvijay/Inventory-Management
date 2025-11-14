package com.microservices.inventory.controller;

import com.microservices.inventory.dto.InventoryItemRequest;
import com.microservices.inventory.dto.InventoryItemResponse;
import com.microservices.inventory.event.LowStockEvent;
import com.microservices.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryItemResponse> getInventoryBySku(@PathVariable String sku) {
        try {
            InventoryItemResponse response = inventoryService.getInventoryBySku(sku);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createInventoryItem(@RequestBody InventoryItemRequest request) {
        try {
            InventoryItemResponse response = inventoryService.createInventoryItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{sku}")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(
            @PathVariable String sku,
            @RequestBody InventoryItemRequest request) {
        try {
            InventoryItemResponse response = inventoryService.updateInventoryItem(sku, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems() {
        List<InventoryItemResponse> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    @GetMapping("/events")
    public ResponseEntity<List<LowStockEvent>> getEventLog() {
        List<LowStockEvent> events = inventoryService.getEventLog();
        return ResponseEntity.ok(events);
    }
}
