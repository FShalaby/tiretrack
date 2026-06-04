package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceStatusUpdateRequest {
    private String status;
    private BigDecimal amountPaid;
    private LocalDate dueDate;
    private String paymentMethod;

    public InvoiceStatusUpdateRequest() {
    }

    public InvoiceStatusUpdateRequest(String status, BigDecimal amountPaid, LocalDate dueDate, String paymentMethod) {
        this.status = status;
        this.amountPaid = amountPaid;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
