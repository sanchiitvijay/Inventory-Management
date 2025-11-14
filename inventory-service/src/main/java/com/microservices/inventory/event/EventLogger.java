package com.microservices.inventory.event;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class EventLogger {

    private final List<LowStockEvent> eventLog = new ArrayList<>();

    public void logLowStockEvent(LowStockEvent event) {
        eventLog.add(event);
        System.out.println("Low stock detected: " + event);
    }

    public List<LowStockEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public void clearEventLog() {
        eventLog.clear();
    }
}
