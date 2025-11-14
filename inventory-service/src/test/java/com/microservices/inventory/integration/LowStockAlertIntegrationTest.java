package com.microservices.inventory.integration;

import com.microservices.inventory.dto.InventoryItemRequest;
import com.microservices.inventory.dto.LowStockAlertResponse;
import com.microservices.inventory.entity.LowStockAlert;
import com.microservices.inventory.repository.LowStockAlertRepository;
import com.microservices.inventory.service.InventoryService;
import com.microservices.inventory.service.LowStockAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LowStockAlertIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private LowStockAlertService alertService;

    @Autowired
    private LowStockAlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
    }

    @Test
    void testAlertCreation_WhenInventoryCreatedWithLowStock() {
        // Create inventory item with low stock
        InventoryItemRequest request = new InventoryItemRequest("LOW-SKU-001", 5, 10);
        inventoryService.createInventoryItem(request);

        // Verify alert was created
        List<LowStockAlertResponse> alerts = alertService.getAllAlerts();
        assertEquals(1, alerts.size());

        LowStockAlertResponse alert = alerts.get(0);
        assertEquals("LOW-SKU-001", alert.getSku());
        assertEquals(5, alert.getAvailableQuantity());
        assertEquals(10, alert.getThreshold());
        assertNotNull(alert.getTimestamp());
    }

    @Test
    void testAlertCreation_WhenStockDeducted() {
        // Create inventory item with sufficient stock
        InventoryItemRequest request = new InventoryItemRequest("STOCK-001", 100, 20);
        inventoryService.createInventoryItem(request);

        // Clear alerts from creation
        alertRepository.deleteAll();

        // Deduct stock to trigger low stock alert
        inventoryService.deductStock("STOCK-001", 85);

        // Verify alert was created
        List<LowStockAlertResponse> alerts = alertService.getAllAlerts();
        assertEquals(1, alerts.size());

        LowStockAlertResponse alert = alerts.get(0);
        assertEquals("STOCK-001", alert.getSku());
        assertEquals(15, alert.getAvailableQuantity());
        assertEquals(20, alert.getThreshold());
    }

    @Test
    void testMultipleAlerts_ForSameSku() {
        // Create item with low stock
        InventoryItemRequest request = new InventoryItemRequest("MULTI-001", 15, 20);
        inventoryService.createInventoryItem(request);

        // Update to trigger another alert
        InventoryItemRequest updateRequest = new InventoryItemRequest();
        updateRequest.setAvailable(10);
        inventoryService.updateInventoryItem("MULTI-001", updateRequest);

        // Verify multiple alerts for same SKU
        List<LowStockAlertResponse> alerts = alertService.getAlertsBySku("MULTI-001");
        assertEquals(2, alerts.size());
        assertTrue(alerts.stream().allMatch(a -> a.getSku().equals("MULTI-001")));
    }

    @Test
    void testGetAlertsBySku_FiltersBySkuCorrectly() {
        // Create multiple items with low stock
        inventoryService.createInventoryItem(new InventoryItemRequest("SKU-A", 5, 10));
        inventoryService.createInventoryItem(new InventoryItemRequest("SKU-B", 3, 8));
        inventoryService.createInventoryItem(new InventoryItemRequest("SKU-C", 2, 5));

        // Verify filtering by SKU
        List<LowStockAlertResponse> alertsA = alertService.getAlertsBySku("SKU-A");
        assertEquals(1, alertsA.size());
        assertEquals("SKU-A", alertsA.get(0).getSku());

        List<LowStockAlertResponse> alertsB = alertService.getAlertsBySku("SKU-B");
        assertEquals(1, alertsB.size());
        assertEquals("SKU-B", alertsB.get(0).getSku());
    }

    @Test
    void testGetAllAlerts_OrderedByTimestampDesc() throws InterruptedException {
        // Create alerts with small time gaps
        inventoryService.createInventoryItem(new InventoryItemRequest("FIRST", 5, 10));
        Thread.sleep(10);
        inventoryService.createInventoryItem(new InventoryItemRequest("SECOND", 3, 8));
        Thread.sleep(10);
        inventoryService.createInventoryItem(new InventoryItemRequest("THIRD", 2, 5));

        // Verify order (most recent first)
        List<LowStockAlertResponse> alerts = alertService.getAllAlerts();
        assertEquals(3, alerts.size());
        
        // Most recent should be first
        assertTrue(alerts.get(0).getTimestamp().isAfter(alerts.get(1).getTimestamp()) ||
                   alerts.get(0).getTimestamp().equals(alerts.get(1).getTimestamp()));
        assertTrue(alerts.get(1).getTimestamp().isAfter(alerts.get(2).getTimestamp()) ||
                   alerts.get(1).getTimestamp().equals(alerts.get(2).getTimestamp()));
    }

    @Test
    void testAlertCount() {
        // Create multiple low stock items
        inventoryService.createInventoryItem(new InventoryItemRequest("COUNT-1", 5, 10));
        inventoryService.createInventoryItem(new InventoryItemRequest("COUNT-2", 3, 8));
        inventoryService.createInventoryItem(new InventoryItemRequest("COUNT-3", 2, 5));

        long count = alertService.getAlertCount();
        assertEquals(3, count);
    }

    @Test
    void testNoAlert_WhenStockAboveThreshold() {
        // Create inventory with sufficient stock
        InventoryItemRequest request = new InventoryItemRequest("HIGH-STOCK", 100, 20);
        inventoryService.createInventoryItem(request);

        // Clear creation alert if any
        long initialCount = alertService.getAlertCount();

        // Deduct small amount keeping stock above threshold
        inventoryService.deductStock("HIGH-STOCK", 10);

        // Verify no new alert was created
        assertEquals(initialCount, alertService.getAlertCount());
    }

    @Test
    void testDeleteAllAlerts() {
        // Create some alerts
        inventoryService.createInventoryItem(new InventoryItemRequest("DEL-1", 5, 10));
        inventoryService.createInventoryItem(new InventoryItemRequest("DEL-2", 3, 8));

        assertEquals(2, alertService.getAlertCount());

        // Delete all alerts
        alertService.deleteAllAlerts();

        assertEquals(0, alertService.getAlertCount());
        assertTrue(alertService.getAllAlerts().isEmpty());
    }
}
