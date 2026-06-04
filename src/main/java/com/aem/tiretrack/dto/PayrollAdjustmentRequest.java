package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.PayrollAdjustmentType;

public class PayrollAdjustmentRequest {
    private PayrollAdjustmentType type;
    private BigDecimal amount;
    private String notes;
    private Long employeeLoanId;

    public PayrollAdjustmentType getType() { return type; }
    public void setType(PayrollAdjustmentType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getEmployeeLoanId() { return employeeLoanId; }
    public void setEmployeeLoanId(Long employeeLoanId) { this.employeeLoanId = employeeLoanId; }
}
