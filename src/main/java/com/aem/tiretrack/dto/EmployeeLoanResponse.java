package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.LoanStatus;
import com.aem.tiretrack.model.EmployeeLoan;
import com.aem.tiretrack.model.User;

public class EmployeeLoanResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private BigDecimal originalAmount;
    private BigDecimal remainingBalance;
    private BigDecimal installmentAmount;
    private LoanStatus status;
    private String notes;
    private Long shopId;
    private String shopName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeLoanResponse(EmployeeLoan loan) {
        User employee = loan.getEmployee();

        this.id = loan.getId();
        this.employeeId = employee == null ? null : employee.getId();
        this.employeeName = employee == null ? null : employee.getFullName();
        this.employeeEmail = employee == null ? null : employee.getEmail();
        this.originalAmount = loan.getOriginalAmount();
        this.remainingBalance = loan.getRemainingBalance();
        this.installmentAmount = loan.getInstallmentAmount();
        this.status = loan.getStatus();
        this.notes = loan.getNotes();
        this.shopId = loan.getShopId();
        this.shopName = loan.getShopName();
        this.createdAt = loan.getCreatedAt();
        this.updatedAt = loan.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public BigDecimal getInstallmentAmount() { return installmentAmount; }
    public LoanStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
