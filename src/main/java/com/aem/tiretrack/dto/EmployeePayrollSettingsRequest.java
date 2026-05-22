package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.EmploymentType;

public class EmployeePayrollSettingsRequest {
    private boolean payrollEnabled;
    private BigDecimal hourlyRate;
    private EmploymentType employmentType;

    public boolean isPayrollEnabled() {
        return payrollEnabled;
    }

    public void setPayrollEnabled(boolean payrollEnabled) {
        this.payrollEnabled = payrollEnabled;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }
}
