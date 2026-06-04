package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.PayrollAdjustmentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll_adjustments")
public class PayrollAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_record_id", nullable = false)
    private PayrollRecord payrollRecord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollAdjustmentType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_loan_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "employee" })
    private EmployeeLoan employeeLoan;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public PayrollRecord getPayrollRecord() { return payrollRecord; }
    public void setPayrollRecord(PayrollRecord payrollRecord) { this.payrollRecord = payrollRecord; }
    public PayrollAdjustmentType getType() { return type; }
    public void setType(PayrollAdjustmentType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public EmployeeLoan getEmployeeLoan() { return employeeLoan; }
    public void setEmployeeLoan(EmployeeLoan employeeLoan) { this.employeeLoan = employeeLoan; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public Long getEmployeeLoanId() {
        return employeeLoan == null ? null : employeeLoan.getId();
    }
}
