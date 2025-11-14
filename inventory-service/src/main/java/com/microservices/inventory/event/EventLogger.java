package com.microservices.inventory.event;

import com.microservices.inventory.entity.LowStockAlert;
import com.microservices.inventory.repository.LowStockAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class EventLogger {

    private static final Logger log = LoggerFactory.getLogger(EventLogger.class);
    
    private final List<LowStockEvent> eventLog = new ArrayList<>();
    private final LowStockAlertRepository alertRepository;

    public EventLogger(LowStockAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void logLowStockEvent(LowStockEvent event) {
        eventLog.add(event);
        
        // Structured logging
        log.warn("Low stock alert: sku={}, available={}, threshold={}, timestamp={}", 
                event.getProductSku(), 
                event.getAvailableQuantity(), 
                event.getThreshold(), 
                event.getTimestamp());
        
        // Save to database
        LowStockAlert alert = new LowStockAlert(
                event.getProductSku(),
                event.getAvailableQuantity(),
                event.getThreshold()
        );
        alertRepository.save(alert);
    }

    public List<LowStockEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public void clearEventLog() {
        eventLog.clear();
    }
}
