package com.aem.tiretrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PayrollShiftSlotResponse {
    private Long id;
    private Long payrollPeriodId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredEmployees;
    private int filledCount;
    private int spotsLeft;
    private String notes;
    private LocalDateTime createdAt;
    private boolean signedUpByCurrentUser;
    private List<SignupSummary> signups;

    public PayrollShiftSlotResponse(Long id, Long payrollPeriodId, LocalDate shiftDate, LocalTime startTime, LocalTime endTime, Integer requiredEmployees, int filledCount, String notes, LocalDateTime createdAt, boolean signedUpByCurrentUser, List<SignupSummary> signups) {
        this.id = id;
        this.payrollPeriodId = payrollPeriodId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredEmployees = requiredEmployees;
        this.filledCount = filledCount;
        this.spotsLeft = Math.max((requiredEmployees == null ? 0 : requiredEmployees) - filledCount, 0);
        this.notes = notes;
        this.createdAt = createdAt;
        this.signedUpByCurrentUser = signedUpByCurrentUser;
        this.signups = signups;
    }

    public Long getId() { return id; }
    public Long getPayrollPeriodId() { return payrollPeriodId; }
    public LocalDate getShiftDate() { return shiftDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Integer getRequiredEmployees() { return requiredEmployees; }
    public int getFilledCount() { return filledCount; }
    public int getSpotsLeft() { return spotsLeft; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isSignedUpByCurrentUser() { return signedUpByCurrentUser; }
    public List<SignupSummary> getSignups() { return signups; }

    public static class SignupSummary {
        private Long id;
        private Long employeeId;
        private String fullName;
        private String email;
        private LocalDateTime createdAt;

        public SignupSummary(Long id, Long employeeId, String fullName, String email, LocalDateTime createdAt) {
            this.id = id;
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getEmployeeId() { return employeeId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
