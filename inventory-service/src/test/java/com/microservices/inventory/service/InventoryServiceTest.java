package com.microservices.inventory.service;

import com.microservices.inventory.dto.InventoryItemRequest;
import com.microservices.inventory.dto.InventoryItemResponse;
import com.microservices.inventory.entity.InventoryItem;
import com.microservices.inventory.event.EventLogger;
import com.microservices.inventory.event.LowStockEvent;
import com.microservices.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventLogger eventLogger;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new InventoryItem("TEST-SKU-001", 100, 20);
        testItem.setId(1L);
    }

    @Test
    void testGetInventoryBySku_Success() {
        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        InventoryItemResponse response = inventoryService.getInventoryBySku("TEST-SKU-001");

        assertNotNull(response);
        assertEquals("TEST-SKU-001", response.getProductSku());
        assertEquals(100, response.getAvailable());
        assertEquals(20, response.getThreshold());
        assertFalse(response.isLowStock());
        verify(inventoryRepository, times(1)).findByProductSku("TEST-SKU-001");
    }

    @Test
    void testGetInventoryBySku_NotFound() {
        when(inventoryRepository.findByProductSku("NON-EXISTENT"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            inventoryService.getInventoryBySku("NON-EXISTENT");
        });
        verify(inventoryRepository, times(1)).findByProductSku("NON-EXISTENT");
    }

    @Test
    void testCreateInventoryItem_Success() {
        InventoryItemRequest request = new InventoryItemRequest("NEW-SKU-001", 50, 10);
        InventoryItem newItem = new InventoryItem("NEW-SKU-001", 50, 10);
        newItem.setId(2L);

        when(inventoryRepository.findByProductSku("NEW-SKU-001"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(newItem);

        InventoryItemResponse response = inventoryService.createInventoryItem(request);

        assertNotNull(response);
        assertEquals("NEW-SKU-001", response.getProductSku());
        assertEquals(50, response.getAvailable());
        assertEquals(10, response.getThreshold());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    void testCreateInventoryItem_LowStockDetection() {
        // Create item with available <= threshold
        InventoryItemRequest request = new InventoryItemRequest("LOW-STOCK-001", 5, 10);
        InventoryItem lowStockItem = new InventoryItem("LOW-STOCK-001", 5, 10);
        lowStockItem.setId(3L);

        when(inventoryRepository.findByProductSku("LOW-STOCK-001"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(lowStockItem);

        InventoryItemResponse response = inventoryService.createInventoryItem(request);

        assertTrue(response.isLowStock());
        // Verify that alert creation is triggered
        verify(eventLogger, times(1)).logLowStockEvent(any(LowStockEvent.class));
    }

    @Test
    void testCreateInventoryItem_DuplicateSku() {
        InventoryItemRequest request = new InventoryItemRequest("TEST-SKU-001", 50, 10);

        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        assertThrows(RuntimeException.class, () -> {
            inventoryService.createInventoryItem(request);
        });
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testUpdateInventoryItem_Success() {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setAvailable(150);
        request.setThreshold(30);

        InventoryItem updatedItem = new InventoryItem("TEST-SKU-001", 150, 30);
        updatedItem.setId(1L);

        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(updatedItem);

        InventoryItemResponse response = inventoryService.updateInventoryItem("TEST-SKU-001", request);

        assertNotNull(response);
        assertEquals(150, response.getAvailable());
        assertEquals(30, response.getThreshold());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    void testDeductStock_Success() {
        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        InventoryItem updatedItem = new InventoryItem("TEST-SKU-001", 70, 20);
        updatedItem.setId(1L);

        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(updatedItem);

        inventoryService.deductStock("TEST-SKU-001", 30);

        verify(inventoryRepository, times(1)).findByProductSku("TEST-SKU-001");
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        verify(eventLogger, never()).logLowStockEvent(any(LowStockEvent.class));
    }

    @Test
    void testDeductStock_InsufficientStock() {
        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock("TEST-SKU-001", 150);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testDeductStock_TriggersLowStockEvent() {
        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        // After deducting 85, available will be 15 which is <= threshold (20)
        InventoryItem lowStockItem = new InventoryItem("TEST-SKU-001", 15, 20);
        lowStockItem.setId(1L);

        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(lowStockItem);

        inventoryService.deductStock("TEST-SKU-001", 85);

        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        // Verify alert is created when stock goes below threshold
        verify(eventLogger, times(1)).logLowStockEvent(any(LowStockEvent.class));
    }

    @Test
    void testDeductStock_ExactlyAtThreshold() {
        when(inventoryRepository.findByProductSku("TEST-SKU-001"))
                .thenReturn(Optional.of(testItem));

        // After deducting 80, available will be 20 which equals threshold (20)
        InventoryItem atThresholdItem = new InventoryItem("TEST-SKU-001", 20, 20);
        atThresholdItem.setId(1L);

        when(inventoryRepository.save(any(InventoryItem.class)))
                .thenReturn(atThresholdItem);

        inventoryService.deductStock("TEST-SKU-001", 80);

        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        // Verify alert is created when stock equals threshold
        verify(eventLogger, times(1)).logLowStockEvent(any(LowStockEvent.class));
    }

    @Test
    void testDeductStock_ItemNotFound() {
        when(inventoryRepository.findByProductSku("NON-EXISTENT"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock("NON-EXISTENT", 10);
        });
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testGetLowStockItems() {
        InventoryItem lowStock1 = new InventoryItem("LOW-001", 5, 10);
        lowStock1.setId(1L);
        InventoryItem lowStock2 = new InventoryItem("LOW-002", 3, 8);
        lowStock2.setId(2L);
        InventoryItem lowStock3 = new InventoryItem("LOW-003", 15, 15);
        lowStock3.setId(3L);

        List<InventoryItem> lowStockItems = Arrays.asList(lowStock1, lowStock2, lowStock3);

        when(inventoryRepository.findLowStockItems())
                .thenReturn(lowStockItems);

        List<InventoryItemResponse> response = inventoryService.getLowStockItems();

        assertNotNull(response);
        assertEquals(3, response.size());
        assertTrue(response.stream().allMatch(InventoryItemResponse::isLowStock));
        verify(inventoryRepository, times(1)).findLowStockItems();
    }

    @Test
    void testGetLowStockItems_EmptyList() {
        when(inventoryRepository.findLowStockItems())
                .thenReturn(Arrays.asList());

        List<InventoryItemResponse> response = inventoryService.getLowStockItems();

        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(inventoryRepository, times(1)).findLowStockItems();
    }

    @Test
    void testLowStockDetection_BelowThreshold() {
        InventoryItem item = new InventoryItem("TEST", 5, 10);
        assertTrue(item.isLowStock());
    }

    @Test
    void testLowStockDetection_AtThreshold() {
        InventoryItem item = new InventoryItem("TEST", 10, 10);
        assertTrue(item.isLowStock());
    }

    @Test
    void testLowStockDetection_AboveThreshold() {
        InventoryItem item = new InventoryItem("TEST", 15, 10);
        assertFalse(item.isLowStock());
    }
}
