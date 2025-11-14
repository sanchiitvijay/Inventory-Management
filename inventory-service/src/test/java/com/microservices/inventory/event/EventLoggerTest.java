package com.microservices.inventory.event;

import com.microservices.inventory.entity.LowStockAlert;
import com.microservices.inventory.repository.LowStockAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLoggerTest {

    @Mock
    private LowStockAlertRepository alertRepository;

    @InjectMocks
    private EventLogger eventLogger;

    @BeforeEach
    void setUp() {
        eventLogger.clearEventLog();
    }

    @Test
    void testLogLowStockEvent_SavesAlertToDatabase() {
        LowStockEvent event = new LowStockEvent("SKU-001", 5, 10);
        LowStockAlert savedAlert = new LowStockAlert("SKU-001", 5, 10);
        savedAlert.setId(1L);

        when(alertRepository.save(any(LowStockAlert.class))).thenReturn(savedAlert);

        eventLogger.logLowStockEvent(event);

        ArgumentCaptor<LowStockAlert> alertCaptor = ArgumentCaptor.forClass(LowStockAlert.class);
        verify(alertRepository, times(1)).save(alertCaptor.capture());

        LowStockAlert capturedAlert = alertCaptor.getValue();
        assertEquals("SKU-001", capturedAlert.getSku());
        assertEquals(5, capturedAlert.getAvailableQuantity());
        assertEquals(10, capturedAlert.getThreshold());
        assertNotNull(capturedAlert.getTimestamp());
    }

    @Test
    void testLogLowStockEvent_AddsToInMemoryLog() {
        LowStockEvent event = new LowStockEvent("SKU-001", 5, 10);

        when(alertRepository.save(any(LowStockAlert.class)))
                .thenReturn(new LowStockAlert("SKU-001", 5, 10));

        eventLogger.logLowStockEvent(event);

        assertEquals(1, eventLogger.getEventLog().size());
        assertEquals("SKU-001", eventLogger.getEventLog().get(0).getProductSku());
    }

    @Test
    void testLogLowStockEvent_MultipleEvents() {
        LowStockEvent event1 = new LowStockEvent("SKU-001", 5, 10);
        LowStockEvent event2 = new LowStockEvent("SKU-002", 3, 8);
        LowStockEvent event3 = new LowStockEvent("SKU-003", 1, 5);

        when(alertRepository.save(any(LowStockAlert.class)))
                .thenReturn(new LowStockAlert());

        eventLogger.logLowStockEvent(event1);
        eventLogger.logLowStockEvent(event2);
        eventLogger.logLowStockEvent(event3);

        assertEquals(3, eventLogger.getEventLog().size());
        verify(alertRepository, times(3)).save(any(LowStockAlert.class));
    }

    @Test
    void testGetEventLog_ReturnsUnmodifiableList() {
        LowStockEvent event = new LowStockEvent("SKU-001", 5, 10);

        when(alertRepository.save(any(LowStockAlert.class)))
                .thenReturn(new LowStockAlert());

        eventLogger.logLowStockEvent(event);

        assertThrows(UnsupportedOperationException.class, () -> {
            eventLogger.getEventLog().clear();
        });
    }

    @Test
    void testClearEventLog() {
        LowStockEvent event1 = new LowStockEvent("SKU-001", 5, 10);
        LowStockEvent event2 = new LowStockEvent("SKU-002", 3, 8);

        when(alertRepository.save(any(LowStockAlert.class)))
                .thenReturn(new LowStockAlert());

        eventLogger.logLowStockEvent(event1);
        eventLogger.logLowStockEvent(event2);
        assertEquals(2, eventLogger.getEventLog().size());

        eventLogger.clearEventLog();
        assertEquals(0, eventLogger.getEventLog().size());
    }

    @Test
    void testLogLowStockEvent_VerifyAlertFields() {
        LowStockEvent event = new LowStockEvent("TEST-SKU", 12, 20);

        when(alertRepository.save(any(LowStockAlert.class)))
                .thenReturn(new LowStockAlert());

        eventLogger.logLowStockEvent(event);

        ArgumentCaptor<LowStockAlert> captor = ArgumentCaptor.forClass(LowStockAlert.class);
        verify(alertRepository).save(captor.capture());

        LowStockAlert alert = captor.getValue();
        assertEquals("TEST-SKU", alert.getSku());
        assertEquals(12, alert.getAvailableQuantity());
        assertEquals(20, alert.getThreshold());
    }
}
