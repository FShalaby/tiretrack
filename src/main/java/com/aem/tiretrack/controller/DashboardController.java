package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.DashboardSummary;
import com.aem.tiretrack.dto.SalesData;
import com.aem.tiretrack.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardSummary getDashboardSummary(@RequestParam(required = false) Long locationId) {
        return dashboardService.getDashboardSummary(locationId);
    }

    @GetMapping("/sales")
    public List<SalesData> getRecentSales(
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(required = false) Long locationId) {
        return dashboardService.getRecentSales(days, locationId);
    }
}
