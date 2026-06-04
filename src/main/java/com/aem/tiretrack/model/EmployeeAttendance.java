package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AbsenceDecision;
import com.aem.tiretrack.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "employee_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "work_date" })
)
public class EmployeeAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation shopLocation;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "worked_hours", precision = 6, scale = 2)
    private BigDecimal workedHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "absence_decision", nullable = false)
    private AbsenceDecision absenceDecision = AbsenceDecision.UNRESOLVED;

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

        if (status == null) {
            status = AttendanceStatus.PRESENT;
        }

        if (absenceDecision == null) {
            absenceDecision = AbsenceDecision.UNRESOLVED;
        }

        if (workedHours == null) {
            workedHours = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (workedHours == null) {
            workedHours = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public ShopLocation getShopLocation() { return shopLocation; }
    public void setShopLocation(ShopLocation shopLocation) { this.shopLocation = shopLocation; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
    public BigDecimal getWorkedHours() { return workedHours; }
    public void setWorkedHours(BigDecimal workedHours) { this.workedHours = workedHours; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public AbsenceDecision getAbsenceDecision() { return absenceDecision; }
    public void setAbsenceDecision(AbsenceDecision absenceDecision) { this.absenceDecision = absenceDecision; }
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

    public Long getLocationId() {
        return shopLocation == null ? null : shopLocation.getId();
    }

    public String getLocationName() {
        return shopLocation == null ? null : shopLocation.getName();
    }
}
