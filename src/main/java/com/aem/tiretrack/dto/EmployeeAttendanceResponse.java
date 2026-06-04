package com.aem.tiretrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AbsenceDecision;
import com.aem.tiretrack.enums.AttendanceStatus;
import com.aem.tiretrack.model.EmployeeAttendance;

public class EmployeeAttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private BigDecimal workedHours;
    private AttendanceStatus status;
    private AbsenceDecision absenceDecision;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeAttendanceResponse(EmployeeAttendance attendance) {
        this.id = attendance.getId();
        this.employeeId = attendance.getEmployeeId();
        this.employeeName = attendance.getEmployeeName();
        this.shopId = attendance.getShopId();
        this.shopName = attendance.getShopName();
        this.locationId = attendance.getLocationId();
        this.locationName = attendance.getLocationName();
        this.workDate = attendance.getWorkDate();
        this.clockIn = attendance.getClockIn();
        this.clockOut = attendance.getClockOut();
        this.workedHours = attendance.getWorkedHours();
        this.status = attendance.getStatus();
        this.absenceDecision = attendance.getAbsenceDecision();
        this.notes = attendance.getNotes();
        this.createdAt = attendance.getCreatedAt();
        this.updatedAt = attendance.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public BigDecimal getWorkedHours() { return workedHours; }
    public AttendanceStatus getStatus() { return status; }
    public AbsenceDecision getAbsenceDecision() { return absenceDecision; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
