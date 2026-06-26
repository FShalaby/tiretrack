package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.BillingCycle;
import com.aem.tiretrack.enums.ShopSubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_subscriptions")
public class ShopSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(name = "plan_name", nullable = false)
    private String planName = "TireTrack Professional";

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents = 39900L;

    @Column(nullable = false)
    private String currency = "CAD";

    @Column(name = "tax_rate", precision = 8, scale = 4)
    private BigDecimal taxRate = new BigDecimal("0.1300");

    @Column(name = "tax_cents", nullable = false)
    private Long taxCents = 0L;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents = 39900L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopSubscriptionStatus status = ShopSubscriptionStatus.TRIAL;

    @Column(name = "demo_mode", nullable = false)
    private boolean demoMode = false;

    @Column(name = "demo_multi_location", nullable = false)
    private boolean demoMultiLocation = false;

    @Column(name = "trial_start_date")
    private LocalDate trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "current_period_start")
    private LocalDate currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDate currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    private boolean cancelAtPeriodEnd = false;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "grace_period_ends_at")
    private LocalDate gracePeriodEndsAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "external_customer_id")
    private String externalCustomerId;

    @Column(name = "external_subscription_id")
    private String externalSubscriptionId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        applyDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (planName == null || planName.isBlank()) {
            planName = "TireTrack Professional";
        }
        if (billingCycle == null) {
            billingCycle = BillingCycle.MONTHLY;
        }
        if (priceCents == null) {
            priceCents = billingCycle == BillingCycle.ANNUAL ? 399900L : 39900L;
        }
        if (currency == null || currency.isBlank()) {
            currency = "CAD";
        }
        if (taxRate == null) {
            taxRate = new BigDecimal("0.1300");
        }
        if (taxCents == null) {
            taxCents = 0L;
        }
        if (totalCents == null) {
            totalCents = priceCents + taxCents;
        }
        if (status == null) {
            status = ShopSubscriptionStatus.TRIAL;
        }
    }

    public Long getId() { return id; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    public Long getPriceCents() { return priceCents; }
    public void setPriceCents(Long priceCents) { this.priceCents = priceCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public Long getTaxCents() { return taxCents; }
    public void setTaxCents(Long taxCents) { this.taxCents = taxCents; }
    public Long getTotalCents() { return totalCents; }
    public void setTotalCents(Long totalCents) { this.totalCents = totalCents; }
    public ShopSubscriptionStatus getStatus() { return status; }
    public void setStatus(ShopSubscriptionStatus status) { this.status = status; }
    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }
    public boolean isDemoMultiLocation() { return demoMultiLocation; }
    public void setDemoMultiLocation(boolean demoMultiLocation) { this.demoMultiLocation = demoMultiLocation; }
    public LocalDate getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDate trialStartDate) { this.trialStartDate = trialStartDate; }
    public LocalDate getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }
    public LocalDate getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(LocalDate currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    public LocalDate getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(LocalDate currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) { this.cancelAtPeriodEnd = cancelAtPeriodEnd; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDate getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public void setGracePeriodEndsAt(LocalDate gracePeriodEndsAt) { this.gracePeriodEndsAt = gracePeriodEndsAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public void setExternalCustomerId(String externalCustomerId) { this.externalCustomerId = externalCustomerId; }
    public String getExternalSubscriptionId() { return externalSubscriptionId; }
    public void setExternalSubscriptionId(String externalSubscriptionId) { this.externalSubscriptionId = externalSubscriptionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
