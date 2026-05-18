package com.aem.tiretrack.dto;

import java.math.BigDecimal;

public class DashboardSummary {

    private int totalTiresInStock;
    private int lowStockCount;
    private long totalInvoices;
    private BigDecimal totalRevenue;
    private long todayAppointments;

    public DashboardSummary(int totalTiresInStock, int lowStockCount, long totalInvoices,
            BigDecimal totalRevenue, long todayAppointments) {
        this.totalTiresInStock = totalTiresInStock;
        this.lowStockCount = lowStockCount;
        this.totalInvoices = totalInvoices;
        this.totalRevenue = totalRevenue;
        this.todayAppointments = todayAppointments;
    }

    public int getTotalTiresInStock() {
        return totalTiresInStock;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }
}
