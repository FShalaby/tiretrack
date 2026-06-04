package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.PayrollAdjustmentType;
import com.aem.tiretrack.model.EmployeeLoan;
import com.aem.tiretrack.model.PayrollAdjustment;

public class PayrollAdjustmentResponse {
    private Long id;
    private PayrollAdjustmentType type;
    private BigDecimal amount;
    private String notes;
    private Long employeeLoanId;
    private BigDecimal employeeLoanRemainingBalance;
    private LocalDateTime createdAt;

    public PayrollAdjustmentResponse(PayrollAdjustment adjustment) {
        EmployeeLoan loan = adjustment.getEmployeeLoan();

        this.id = adjustment.getId();
        this.type = adjustment.getType();
        this.amount = adjustment.getAmount();
        this.notes = adjustment.getNotes();
        this.employeeLoanId = loan == null ? null : loan.getId();
        this.employeeLoanRemainingBalance = loan == null ? null : loan.getRemainingBalance();
        this.createdAt = adjustment.getCreatedAt();
    }

    public Long getId() { return id; }
    public PayrollAdjustmentType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getNotes() { return notes; }
    public Long getEmployeeLoanId() { return employeeLoanId; }
    public BigDecimal getEmployeeLoanRemainingBalance() { return employeeLoanRemainingBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
