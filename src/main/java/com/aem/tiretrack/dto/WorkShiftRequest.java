package com.aem.tiretrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkShiftRequest {

    private Long employeeId;

    private LocalDate shiftDate;

    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private Integer breakMinutes;

    private String notes;

    public WorkShiftRequest() {
    }

    public WorkShiftRequest(Long employeeId,
                            LocalDate shiftDate,
                            LocalDateTime clockIn,
                            LocalDateTime clockOut,
                            Integer breakMinutes,
                            String notes) {
        this.employeeId = employeeId;
        this.shiftDate = shiftDate;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.breakMinutes = breakMinutes;
        this.notes = notes;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public LocalDateTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalDateTime clockIn) {
        this.clockIn = clockIn;
    }

    public LocalDateTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalDateTime clockOut) {
        this.clockOut = clockOut;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(Integer breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}