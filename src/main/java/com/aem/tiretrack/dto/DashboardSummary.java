package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummary {

    private int totalTiresInStock;
    private int lowStockCount;
    private long totalInvoices;
    private BigDecimal totalRevenue;
    private BigDecimal totalInvoiced;
    private BigDecimal totalCollected;
    private BigDecimal outstandingBalance;
    private long partiallyPaidInvoices;
    private long todayAppointments;
    private long pendingWorkOrders;
    private long inProgressWorkOrders;
    private long vehicleReadyWorkOrders;
    private long completedWorkOrdersToday;
    private long totalCustomers;
    private BigDecimal totalPayrollCost;
    private BigDecimal totalExpenses;
    private List<DashboardLocationBreakdown> locationBreakdowns;

    public DashboardSummary(int totalTiresInStock, int lowStockCount, long totalInvoices,
            BigDecimal totalRevenue, BigDecimal totalInvoiced, BigDecimal totalCollected,
            BigDecimal outstandingBalance, long partiallyPaidInvoices, long todayAppointments, long pendingWorkOrders,
            long inProgressWorkOrders, long vehicleReadyWorkOrders, long completedWorkOrdersToday,
            long totalCustomers, BigDecimal totalPayrollCost, BigDecimal totalExpenses,
            List<DashboardLocationBreakdown> locationBreakdowns) {
        this.totalTiresInStock = totalTiresInStock;
        this.lowStockCount = lowStockCount;
        this.totalInvoices = totalInvoices;
        this.totalRevenue = totalRevenue;
        this.totalInvoiced = totalInvoiced;
        this.totalCollected = totalCollected;
        this.outstandingBalance = outstandingBalance;
        this.partiallyPaidInvoices = partiallyPaidInvoices;
        this.todayAppointments = todayAppointments;
        this.pendingWorkOrders = pendingWorkOrders;
        this.inProgressWorkOrders = inProgressWorkOrders;
        this.vehicleReadyWorkOrders = vehicleReadyWorkOrders;
        this.completedWorkOrdersToday = completedWorkOrdersToday;
        this.totalCustomers = totalCustomers;
        this.totalPayrollCost = totalPayrollCost;
        this.totalExpenses = totalExpenses;
        this.locationBreakdowns = locationBreakdowns == null ? List.of() : locationBreakdowns;
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

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public BigDecimal getTotalCollected() {
        return totalCollected;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public long getPartiallyPaidInvoices() {
        return partiallyPaidInvoices;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public long getPendingWorkOrders() {
        return pendingWorkOrders;
    }

    public long getInProgressWorkOrders() {
        return inProgressWorkOrders;
    }

    public long getVehicleReadyWorkOrders() {
        return vehicleReadyWorkOrders;
    }

    public long getCompletedWorkOrdersToday() {
        return completedWorkOrdersToday;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public BigDecimal getTotalPayrollCost() {
        return totalPayrollCost;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public List<DashboardLocationBreakdown> getLocationBreakdowns() {
        return locationBreakdowns;
    }
}
