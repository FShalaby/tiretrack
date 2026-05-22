package com.aem.tiretrack.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PayrollShiftSlotRequest {
    private Long payrollPeriodId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredEmployees;
    private String notes;

    public Long getPayrollPeriodId() { return payrollPeriodId; }
    public void setPayrollPeriodId(Long payrollPeriodId) { this.payrollPeriodId = payrollPeriodId; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getRequiredEmployees() { return requiredEmployees; }
    public void setRequiredEmployees(Integer requiredEmployees) { this.requiredEmployees = requiredEmployees; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
