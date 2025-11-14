package com.microservices.order.controller;

import com.microservices.order.dto.DashboardData;
import com.microservices.order.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardData data = dashboardService.getDashboardData();
        model.addAttribute("data", data);
        return "dashboard";
    }
}
