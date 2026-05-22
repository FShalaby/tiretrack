package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.PayrollStatus;
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
        return regularHours;
    }
    public void setRegularHours(BigDecimal regularHours) {
        this.regularHours = regularHours;
    }
    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }
    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    public BigDecimal getGrossPay() {
        return grossPay;
    }
    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
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

}
