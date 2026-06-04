package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.LoanStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_loans")
public class EmployeeLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "passwordHash" })
    private User employee;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "original_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal originalAmount = BigDecimal.ZERO;

    @Column(name = "remaining_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(name = "installment_amount", precision = 12, scale = 2)
    private BigDecimal installmentAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeMoneyFields();
        if (status == null) {
            status = LoanStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeMoneyFields();
        if (status == null) {
            status = LoanStatus.ACTIVE;
        }
    }

    public Long getId() { return id; }
    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    public BigDecimal getInstallmentAmount() { return installmentAmount; }
    public void setInstallmentAmount(BigDecimal installmentAmount) { this.installmentAmount = installmentAmount; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Long getEmployeeId() {
        return employee == null ? null : employee.getId();
    }

    public String getEmployeeName() {
        return employee == null ? null : employee.getFullName();
    }

    public Long getShopId() {
        return shop == null ? null : shop.getId();
    }

    public String getShopName() {
        return shop == null ? null : shop.getName();
    }

    private void normalizeMoneyFields() {
        if (originalAmount == null) originalAmount = BigDecimal.ZERO;
        if (remainingBalance == null) remainingBalance = originalAmount;
        if (installmentAmount == null) installmentAmount = BigDecimal.ZERO;
    }
}
