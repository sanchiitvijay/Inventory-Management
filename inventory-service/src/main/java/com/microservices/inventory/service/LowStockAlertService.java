package com.microservices.inventory.service;

import com.microservices.inventory.dto.LowStockAlertResponse;
import com.microservices.inventory.entity.LowStockAlert;
import com.microservices.inventory.repository.LowStockAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LowStockAlertService {

    private final LowStockAlertRepository alertRepository;

    public LowStockAlertService(LowStockAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByTimestampDesc().stream()
                .map(LowStockAlertResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getAlertsBySku(String sku) {
        return alertRepository.findBySku(sku).stream()
                .map(LowStockAlertResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getAlertCount() {
        return alertRepository.count();
    }

    @Transactional
    public void deleteAllAlerts() {
        alertRepository.deleteAll();
    }
}
