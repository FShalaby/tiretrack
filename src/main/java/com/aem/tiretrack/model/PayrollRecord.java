package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.aem.tiretrack.enums.PayrollStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll_records")
public class PayrollRecord 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "passwordHash" })
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private PayrollPeriod payrollPeriod;

    @Column(name = "regular_hours", precision = 6, scale = 2)
    private BigDecimal regularHours;
    
    @Column(name = "overtime_hours", precision = 6, scale = 2)
    private BigDecimal overtimeHours;
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "gross_pay", precision = 12, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "bonus_amount", precision = 12, scale = 2)
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @Column(name = "reimbursement_amount", precision = 12, scale = 2)
    private BigDecimal reimbursementAmount = BigDecimal.ZERO;

    @Column(name = "deduction_amount", precision = 12, scale = 2)
    private BigDecimal deductionAmount = BigDecimal.ZERO;

    @Column(name = "penalty_amount", precision = 12, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "loan_deduction_amount", precision = 12, scale = 2)
    private BigDecimal loanDeductionAmount = BigDecimal.ZERO;

    @Column(name = "tax_deduction_amount", precision = 12, scale = 2)
    private BigDecimal taxDeductionAmount = BigDecimal.ZERO;

    @Column(name = "other_deduction_amount", precision = 12, scale = 2)
    private BigDecimal otherDeductionAmount = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay", precision = 12, scale = 2)
    private BigDecimal netPay;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "accounting_synced", nullable = false, columnDefinition = "boolean default false")
    private Boolean accountingSynced = false;

    @Column(name = "accounting_entry_id")
    private Long accountingEntryId;

    @OneToMany(mappedBy = "payrollRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "payrollRecord" })
    private List<PayrollAdjustment> adjustments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @Column(name="paid_at")
    private LocalDateTime paidAt;

    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = PayrollStatus.PENDING;
        }
        normalizeMoneyFields();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeMoneyFields();
    }

    public Long getId() {
        return id;
    }
    
    public User getEmployee() {
        return employee;
    }
    public void setEmployee(User employee) {
        this.employee = employee;
    }
    public PayrollPeriod getPayrollPeriod() {
        return payrollPeriod;
    }
    public void setPayrollPeriod(PayrollPeriod payrollPeriod) {
        this.payrollPeriod = payrollPeriod;
    }
    public BigDecimal getRegularHours() {
        return zero(regularHours);
    }
    public void setRegularHours(BigDecimal regularHours) {
        this.regularHours = regularHours;
    }
    public BigDecimal getOvertimeHours() {
        return zero(overtimeHours);
    }
    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }
    public BigDecimal getHourlyRate() {
        return zero(hourlyRate);
    }
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    public BigDecimal getGrossPay() {
        return zero(grossPay);
    }
    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }
    public BigDecimal getBonusAmount() {
        return zero(bonusAmount);
    }
    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }
    public BigDecimal getReimbursementAmount() {
        return zero(reimbursementAmount);
    }
    public void setReimbursementAmount(BigDecimal reimbursementAmount) {
        this.reimbursementAmount = reimbursementAmount;
    }
    public BigDecimal getDeductionAmount() {
        return zero(deductionAmount);
    }
    public void setDeductionAmount(BigDecimal deductionAmount) {
        this.deductionAmount = deductionAmount;
    }
    public BigDecimal getPenaltyAmount() {
        return zero(penaltyAmount);
    }
    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }
    public BigDecimal getLoanDeductionAmount() {
        return zero(loanDeductionAmount);
    }
    public void setLoanDeductionAmount(BigDecimal loanDeductionAmount) {
        this.loanDeductionAmount = loanDeductionAmount;
    }
    public BigDecimal getTaxDeductionAmount() {
        return zero(taxDeductionAmount);
    }
    public void setTaxDeductionAmount(BigDecimal taxDeductionAmount) {
        this.taxDeductionAmount = taxDeductionAmount;
    }
    public BigDecimal getOtherDeductionAmount() {
        return zero(otherDeductionAmount);
    }
    public void setOtherDeductionAmount(BigDecimal otherDeductionAmount) {
        this.otherDeductionAmount = otherDeductionAmount;
    }
    public BigDecimal getTotalDeductions() {
        return zero(totalDeductions);
    }
    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }
    public BigDecimal getNetPay() {
        return netPay == null ? getGrossPay().subtract(getTotalDeductions()) : netPay;
    }
    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public boolean isAccountingSynced() {
        return accountingSynced != null && accountingSynced;
    }
    public void setAccountingSynced(boolean accountingSynced) {
        this.accountingSynced = accountingSynced;
    }
    public Long getAccountingEntryId() {
        return accountingEntryId;
    }
    public void setAccountingEntryId(Long accountingEntryId) {
        this.accountingEntryId = accountingEntryId;
    }
    public List<PayrollAdjustment> getAdjustments() {
        return adjustments;
    }
    public void setAdjustments(List<PayrollAdjustment> adjustments) {
        this.adjustments.clear();
        if (adjustments != null) {
            adjustments.forEach(this::addAdjustment);
        }
    }
    public void addAdjustment(PayrollAdjustment adjustment) {
        adjustment.setPayrollRecord(this);
        adjustments.add(adjustment);
    }
    public void removeAdjustment(PayrollAdjustment adjustment) {
        adjustments.remove(adjustment);
        adjustment.setPayrollRecord(null);
    }
    public PayrollStatus getStatus() {
        return status;
    }
    public void setStatus(PayrollStatus status) {
        this.status = status;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private void normalizeMoneyFields() {
        if (regularHours == null) regularHours = BigDecimal.ZERO;
        if (overtimeHours == null) overtimeHours = BigDecimal.ZERO;
        if (hourlyRate == null) hourlyRate = BigDecimal.ZERO;
        if (grossPay == null) grossPay = BigDecimal.ZERO;
        if (bonusAmount == null) bonusAmount = BigDecimal.ZERO;
        if (reimbursementAmount == null) reimbursementAmount = BigDecimal.ZERO;
        if (deductionAmount == null) deductionAmount = BigDecimal.ZERO;
        if (penaltyAmount == null) penaltyAmount = BigDecimal.ZERO;
        if (loanDeductionAmount == null) loanDeductionAmount = BigDecimal.ZERO;
        if (taxDeductionAmount == null) taxDeductionAmount = BigDecimal.ZERO;
        if (otherDeductionAmount == null) otherDeductionAmount = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        if (netPay == null) netPay = grossPay.subtract(totalDeductions);
        if (accountingSynced == null) accountingSynced = false;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
