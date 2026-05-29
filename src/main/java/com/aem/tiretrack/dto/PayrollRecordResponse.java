package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.EmploymentType;
import com.aem.tiretrack.enums.PayrollStatus;
import com.aem.tiretrack.model.PayrollPeriod;
import com.aem.tiretrack.model.PayrollRecord;
import com.aem.tiretrack.model.User;

public class PayrollRecordResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private EmploymentType employmentType;
    private boolean payrollEnabled;
    private Long payrollPeriodId;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal hourlyRate;
    private BigDecimal grossPay;
    private PayrollStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PayrollRecordResponse(PayrollRecord record) {
        User employee = record.getEmployee();
        PayrollPeriod period = record.getPayrollPeriod();

        this.id = record.getId();
        this.employeeId = employee == null ? null : employee.getId();
        this.employeeName = employee == null ? null : employee.getFullName();
        this.employeeEmail = employee == null ? null : employee.getEmail();
        this.employmentType = employee == null ? null : employee.getEmploymentType();
        this.payrollEnabled = employee != null && employee.isPayrollEnabled();
        this.payrollPeriodId = period == null ? null : period.getId();
        this.periodStartDate = period == null ? null : period.getStartDate();
        this.periodEndDate = period == null ? null : period.getEndDate();
        this.regularHours = record.getRegularHours();
        this.overtimeHours = record.getOvertimeHours();
        this.hourlyRate = record.getHourlyRate();
        this.grossPay = record.getGrossPay();
        this.status = record.getStatus();
        this.paidAt = record.getPaidAt();
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public boolean isPayrollEnabled() { return payrollEnabled; }
    public Long getPayrollPeriodId() { return payrollPeriodId; }
    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public BigDecimal getRegularHours() { return regularHours; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getGrossPay() { return grossPay; }
    public PayrollStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
