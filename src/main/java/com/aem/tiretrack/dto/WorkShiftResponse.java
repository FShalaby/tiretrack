package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.model.User;
import com.aem.tiretrack.model.WorkShift;

public class WorkShiftResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate shiftDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Integer breakMinutes;
    private BigDecimal workedHours;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkShiftResponse(WorkShift shift) {
        User employee = shift.getEmployee();

        this.id = shift.getId();
        this.employeeId = employee == null ? null : employee.getId();
        this.employeeName = employee == null ? null : employee.getFullName();
        this.shiftDate = shift.getShiftDate();
        this.clockIn = shift.getClockIn();
        this.clockOut = shift.getClockOut();
        this.breakMinutes = shift.getBreakMinutes();
        this.workedHours = shift.getWorkedHours();
        this.notes = shift.getNotes();
        this.createdAt = shift.getCreatedAt();
        this.updatedAt = shift.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getShiftDate() { return shiftDate; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public Integer getBreakMinutes() { return breakMinutes; }
    public BigDecimal getWorkedHours() { return workedHours; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
