package com.aem.tiretrack.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aem.tiretrack.enums.BillingCycle;
import com.aem.tiretrack.enums.ShopSubscriptionStatus;
import com.aem.tiretrack.enums.SubscriptionPlan;

public class ShopSubscriptionRequest {
    private SubscriptionPlan shopPlan;
    private String planName;
    private BillingCycle billingCycle;
    private Long priceCents;
    private String currency;
    private BigDecimal taxRate;
    private Long taxCents;
    private Long totalCents;
    private ShopSubscriptionStatus status;
    private Boolean demoMode;
    private Boolean demoMultiLocation;
    private LocalDate trialStartDate;
    private LocalDate trialEndDate;
    private LocalDate currentPeriodStart;
    private LocalDate currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;
    private LocalDate gracePeriodEndsAt;
    private String notes;
    private String externalCustomerId;
    private String externalSubscriptionId;

    public SubscriptionPlan getShopPlan() { return shopPlan; }
    public void setShopPlan(SubscriptionPlan shopPlan) { this.shopPlan = shopPlan; }
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
    public Boolean getDemoMode() { return demoMode; }
    public void setDemoMode(Boolean demoMode) { this.demoMode = demoMode; }
    public Boolean getDemoMultiLocation() { return demoMultiLocation; }
    public void setDemoMultiLocation(Boolean demoMultiLocation) { this.demoMultiLocation = demoMultiLocation; }
    public LocalDate getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDate trialStartDate) { this.trialStartDate = trialStartDate; }
    public LocalDate getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }
    public LocalDate getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(LocalDate currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    public LocalDate getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(LocalDate currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    public Boolean getCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) { this.cancelAtPeriodEnd = cancelAtPeriodEnd; }
    public LocalDate getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public void setGracePeriodEndsAt(LocalDate gracePeriodEndsAt) { this.gracePeriodEndsAt = gracePeriodEndsAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public void setExternalCustomerId(String externalCustomerId) { this.externalCustomerId = externalCustomerId; }
    public String getExternalSubscriptionId() { return externalSubscriptionId; }
    public void setExternalSubscriptionId(String externalSubscriptionId) { this.externalSubscriptionId = externalSubscriptionId; }
}
