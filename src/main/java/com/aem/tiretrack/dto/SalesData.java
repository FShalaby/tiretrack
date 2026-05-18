package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesData {
    private LocalDate date;
    private BigDecimal revenue;
    private long invoiceCount;

    public SalesData(LocalDate date, BigDecimal revenue, long invoiceCount) {
        this.date = date;
        this.revenue = revenue;
        this.invoiceCount = invoiceCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(long invoiceCount) {
        this.invoiceCount = invoiceCount;
    }
}
