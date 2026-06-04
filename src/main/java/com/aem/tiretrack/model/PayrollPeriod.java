package com.aem.tiretrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.PayrollStatus;
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
@Entity
@Table(name = "payroll_periods")
public class PayrollPeriod 
{   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name="end_date", nullable=false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation shopLocation;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = PayrollStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public PayrollStatus getStatus() {
        return status;
    }
    public void setStatus(PayrollStatus status) {
        this.status = status;
    }
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public ShopLocation getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(ShopLocation shopLocation) {
        this.shopLocation = shopLocation;
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

    public LocalDateTime getPaidAt() {
    return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
}

