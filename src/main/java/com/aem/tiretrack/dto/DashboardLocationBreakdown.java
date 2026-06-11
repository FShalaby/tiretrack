package com.aem.tiretrack.dto;

import java.math.BigDecimal;

public class DashboardLocationBreakdown {
    private final Long locationId;
    private final String locationName;
    private final String locationType;
    private final int inventoryQuantity;
    private final long lowStockCount;
    private final long appointmentCount;
    private final long invoiceCount;
    private final long customerCount;
    private final BigDecimal revenue;
    private final BigDecimal outstandingBalance;
    private final BigDecimal payrollCost;
    private final BigDecimal expenses;

    public DashboardLocationBreakdown(
            Long locationId,
            String locationName,
            String locationType,
            int inventoryQuantity,
            long lowStockCount,
            long appointmentCount,
            long invoiceCount,
            long customerCount,
            BigDecimal revenue,
            BigDecimal outstandingBalance,
            BigDecimal payrollCost,
            BigDecimal expenses) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.locationType = locationType;
        this.inventoryQuantity = inventoryQuantity;
        this.lowStockCount = lowStockCount;
        this.appointmentCount = appointmentCount;
        this.invoiceCount = invoiceCount;
        this.customerCount = customerCount;
        this.revenue = revenue;
        this.outstandingBalance = outstandingBalance;
        this.payrollCost = payrollCost;
        this.expenses = expenses;
    }

    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getLocationType() { return locationType; }
    public int getInventoryQuantity() { return inventoryQuantity; }
    public long getLowStockCount() { return lowStockCount; }
    public long getAppointmentCount() { return appointmentCount; }
    public long getInvoiceCount() { return invoiceCount; }
    public long getCustomerCount() { return customerCount; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public BigDecimal getPayrollCost() { return payrollCost; }
    public BigDecimal getExpenses() { return expenses; }
}
