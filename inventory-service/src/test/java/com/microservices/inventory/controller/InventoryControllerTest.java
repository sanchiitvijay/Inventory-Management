package com.microservices.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventory.dto.InventoryItemRequest;
import com.microservices.inventory.dto.InventoryItemResponse;
import com.microservices.inventory.dto.LowStockAlertResponse;
import com.microservices.inventory.event.LowStockEvent;
import com.microservices.inventory.service.InventoryService;
import com.microservices.inventory.service.LowStockAlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private LowStockAlertService alertService;

    @Test
    void testGetInventoryBySku_Success() throws Exception {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(1L);
        response.setProductSku("TEST-SKU-001");
        response.setAvailable(100);
        response.setThreshold(20);
        response.setLastUpdated(Instant.now());
        response.setLowStock(false);

        when(inventoryService.getInventoryBySku("TEST-SKU-001"))
                .thenReturn(response);

        mockMvc.perform(get("/inventory/TEST-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.available").value(100))
                .andExpect(jsonPath("$.threshold").value(20))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void testGetInventoryBySku_NotFound() throws Exception {
        when(inventoryService.getInventoryBySku("NON-EXISTENT"))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/inventory/NON-EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateInventoryItem_Success() throws Exception {
        InventoryItemRequest request = new InventoryItemRequest("NEW-SKU-001", 50, 10);

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(2L);
        response.setProductSku("NEW-SKU-001");
        response.setAvailable(50);
        response.setThreshold(10);
        response.setLastUpdated(Instant.now());
        response.setLowStock(false);

        when(inventoryService.createInventoryItem(any(InventoryItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productSku").value("NEW-SKU-001"))
                .andExpect(jsonPath("$.available").value(50))
                .andExpect(jsonPath("$.threshold").value(10));
    }

    @Test
    void testCreateInventoryItem_BadRequest() throws Exception {
        InventoryItemRequest request = new InventoryItemRequest("DUPLICATE-SKU", 50, 10);

        when(inventoryService.createInventoryItem(any(InventoryItemRequest.class)))
                .thenThrow(new RuntimeException("Duplicate SKU"));

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateInventoryItem_Success() throws Exception {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setAvailable(150);
        request.setThreshold(30);

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(1L);
        response.setProductSku("TEST-SKU-001");
        response.setAvailable(150);
        response.setThreshold(30);
        response.setLastUpdated(Instant.now());
        response.setLowStock(false);

        when(inventoryService.updateInventoryItem(eq("TEST-SKU-001"), any(InventoryItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/inventory/TEST-SKU-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(150))
                .andExpect(jsonPath("$.threshold").value(30));
    }

    @Test
    void testUpdateInventoryItem_NotFound() throws Exception {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setAvailable(150);

        when(inventoryService.updateInventoryItem(eq("NON-EXISTENT"), any(InventoryItemRequest.class)))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/inventory/NON-EXISTENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLowStockItems() throws Exception {
        InventoryItemResponse item1 = new InventoryItemResponse();
        item1.setId(1L);
        item1.setProductSku("LOW-001");
        item1.setAvailable(5);
        item1.setThreshold(10);
        item1.setLowStock(true);

        InventoryItemResponse item2 = new InventoryItemResponse();
        item2.setId(2L);
        item2.setProductSku("LOW-002");
        item2.setAvailable(3);
        item2.setThreshold(8);
        item2.setLowStock(true);

        List<InventoryItemResponse> lowStockItems = Arrays.asList(item1, item2);

        when(inventoryService.getLowStockItems())
                .thenReturn(lowStockItems);

        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productSku").value("LOW-001"))
                .andExpect(jsonPath("$[0].lowStock").value(true))
                .andExpect(jsonPath("$[1].productSku").value("LOW-002"))
                .andExpect(jsonPath("$[1].lowStock").value(true));
    }

    @Test
    void testGetEventLog() throws Exception {
        LowStockEvent event1 = new LowStockEvent("LOW-001", 5, 10);
        LowStockEvent event2 = new LowStockEvent("LOW-002", 3, 8);

        List<LowStockEvent> events = Arrays.asList(event1, event2);

        when(inventoryService.getEventLog())
                .thenReturn(events);

        mockMvc.perform(get("/inventory/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productSku").value("LOW-001"))
                .andExpect(jsonPath("$[0].availableQuantity").value(5))
                .andExpect(jsonPath("$[1].productSku").value("LOW-002"))
                .andExpect(jsonPath("$[1].availableQuantity").value(3));
    }

    @Test
    void testGetAllAlerts_Success() throws Exception {
        LowStockAlertResponse alert1 = new LowStockAlertResponse();
        alert1.setId(1L);
        alert1.setSku("SKU-001");
        alert1.setAvailableQuantity(5);
        alert1.setThreshold(10);
        alert1.setTimestamp(Instant.now());

        LowStockAlertResponse alert2 = new LowStockAlertResponse();
        alert2.setId(2L);
        alert2.setSku("SKU-002");
        alert2.setAvailableQuantity(3);
        alert2.setThreshold(8);
        alert2.setTimestamp(Instant.now());

        List<LowStockAlertResponse> alerts = Arrays.asList(alert1, alert2);

        when(alertService.getAllAlerts())
                .thenReturn(alerts);

        mockMvc.perform(get("/inventory/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[0].availableQuantity").value(5))
                .andExpect(jsonPath("$[0].threshold").value(10))
                .andExpect(jsonPath("$[1].sku").value("SKU-002"))
                .andExpect(jsonPath("$[1].availableQuantity").value(3));
    }

    @Test
    void testGetAllAlerts_EmptyList() throws Exception {
        when(alertService.getAllAlerts())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/inventory/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAlertsBySku_Success() throws Exception {
        LowStockAlertResponse alert1 = new LowStockAlertResponse();
        alert1.setId(1L);
        alert1.setSku("TEST-SKU");
        alert1.setAvailableQuantity(5);
        alert1.setThreshold(10);
        alert1.setTimestamp(Instant.now());

        LowStockAlertResponse alert2 = new LowStockAlertResponse();
        alert2.setId(2L);
        alert2.setSku("TEST-SKU");
        alert2.setAvailableQuantity(2);
        alert2.setThreshold(10);
        alert2.setTimestamp(Instant.now());

        List<LowStockAlertResponse> alerts = Arrays.asList(alert1, alert2);

        when(alertService.getAlertsBySku("TEST-SKU"))
                .thenReturn(alerts);

        mockMvc.perform(get("/inventory/alerts/TEST-SKU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sku").value("TEST-SKU"))
                .andExpect(jsonPath("$[1].sku").value("TEST-SKU"));
    }

    @Test
    void testGetAlertsBySku_NotFound() throws Exception {
        when(alertService.getAlertsBySku("NON-EXISTENT"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/inventory/alerts/NON-EXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
