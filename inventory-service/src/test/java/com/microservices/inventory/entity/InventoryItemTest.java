package com.microservices.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {

    @Test
    void testIsLowStock_BelowThreshold() {
        InventoryItem item = new InventoryItem("TEST-SKU", 5, 10);
        assertTrue(item.isLowStock());
    }

    @Test
    void testIsLowStock_AtThreshold() {
        InventoryItem item = new InventoryItem("TEST-SKU", 10, 10);
        assertTrue(item.isLowStock());
    }

    @Test
    void testIsLowStock_AboveThreshold() {
        InventoryItem item = new InventoryItem("TEST-SKU", 15, 10);
        assertFalse(item.isLowStock());
    }

    @Test
    void testIsLowStock_NullValues() {
        InventoryItem item = new InventoryItem();
        assertFalse(item.isLowStock());
    }

    @Test
    void testConstructor() {
        InventoryItem item = new InventoryItem("TEST-SKU-001", 100, 20);
        
        assertEquals("TEST-SKU-001", item.getProductSku());
        assertEquals(100, item.getAvailable());
        assertEquals(20, item.getThreshold());
        assertNotNull(item.getLastUpdated());
    }

    @Test
    void testGettersAndSetters() {
        InventoryItem item = new InventoryItem();
        
        item.setId(1L);
        item.setProductSku("TEST-SKU");
        item.setAvailable(50);
        item.setThreshold(10);
        
        assertEquals(1L, item.getId());
        assertEquals("TEST-SKU", item.getProductSku());
        assertEquals(50, item.getAvailable());
        assertEquals(10, item.getThreshold());
    }
}
