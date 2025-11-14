package com.microservices.inventory.service;

import com.microservices.inventory.dto.LowStockAlertResponse;
import com.microservices.inventory.entity.LowStockAlert;
import com.microservices.inventory.repository.LowStockAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LowStockAlertServiceTest {

    @Mock
    private LowStockAlertRepository alertRepository;

    @InjectMocks
    private LowStockAlertService alertService;

    private LowStockAlert alert1;
    private LowStockAlert alert2;
    private LowStockAlert alert3;

    @BeforeEach
    void setUp() {
        alert1 = new LowStockAlert("SKU-001", 5, 10);
        alert1.setId(1L);

        alert2 = new LowStockAlert("SKU-002", 3, 8);
        alert2.setId(2L);

        alert3 = new LowStockAlert("SKU-001", 2, 10);
        alert3.setId(3L);
    }

    @Test
    void testGetAllAlerts_Success() {
        List<LowStockAlert> alerts = Arrays.asList(alert3, alert2, alert1);
        when(alertRepository.findAllByOrderByTimestampDesc()).thenReturn(alerts);

        List<LowStockAlertResponse> result = alertService.getAllAlerts();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("SKU-001", result.get(0).getSku());
        assertEquals("SKU-002", result.get(1).getSku());
        assertEquals("SKU-001", result.get(2).getSku());
        verify(alertRepository, times(1)).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetAllAlerts_EmptyList() {
        when(alertRepository.findAllByOrderByTimestampDesc()).thenReturn(Collections.emptyList());

        List<LowStockAlertResponse> result = alertService.getAllAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertRepository, times(1)).findAllByOrderByTimestampDesc();
    }

    @Test
    void testGetAlertsBySku_Success() {
        List<LowStockAlert> alerts = Arrays.asList(alert1, alert3);
        when(alertRepository.findBySku("SKU-001")).thenReturn(alerts);

        List<LowStockAlertResponse> result = alertService.getAlertsBySku("SKU-001");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("SKU-001", result.get(0).getSku());
        assertEquals("SKU-001", result.get(1).getSku());
        verify(alertRepository, times(1)).findBySku("SKU-001");
    }

    @Test
    void testGetAlertsBySku_NotFound() {
        when(alertRepository.findBySku("NON-EXISTENT")).thenReturn(Collections.emptyList());

        List<LowStockAlertResponse> result = alertService.getAlertsBySku("NON-EXISTENT");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertRepository, times(1)).findBySku("NON-EXISTENT");
    }

    @Test
    void testGetAlertCount() {
        when(alertRepository.count()).thenReturn(5L);

        long count = alertService.getAlertCount();

        assertEquals(5L, count);
        verify(alertRepository, times(1)).count();
    }

    @Test
    void testGetAlertCount_NoAlerts() {
        when(alertRepository.count()).thenReturn(0L);

        long count = alertService.getAlertCount();

        assertEquals(0L, count);
        verify(alertRepository, times(1)).count();
    }

    @Test
    void testDeleteAllAlerts() {
        doNothing().when(alertRepository).deleteAll();

        alertService.deleteAllAlerts();

        verify(alertRepository, times(1)).deleteAll();
    }

    @Test
    void testLowStockAlertResponseMapping() {
        LowStockAlert alert = new LowStockAlert("TEST-SKU", 7, 15);
        alert.setId(10L);

        LowStockAlertResponse response = new LowStockAlertResponse(alert);

        assertEquals(10L, response.getId());
        assertEquals("TEST-SKU", response.getSku());
        assertEquals(7, response.getAvailableQuantity());
        assertEquals(15, response.getThreshold());
        assertNotNull(response.getTimestamp());
    }
}
