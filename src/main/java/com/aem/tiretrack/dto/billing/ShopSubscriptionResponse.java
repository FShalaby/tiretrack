package com.aem.tiretrack.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.BillingCycle;
import com.aem.tiretrack.enums.ShopSubscriptionStatus;
import com.aem.tiretrack.enums.SubscriptionPlan;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopSubscription;

public class ShopSubscriptionResponse {
    private final boolean configured;
    private final Long id;
    private final Long shopId;
    private final String shopName;
    private final boolean shopActive;
    private final SubscriptionPlan shopPlan;
    private final boolean multiLocationAllowed;
    private final String ownerName;
    private final String ownerEmail;
    private final String planName;
    private final BillingCycle billingCycle;
    private final Long priceCents;
    private final String currency;
    private final BigDecimal taxRate;
    private final Long taxCents;
    private final Long totalCents;
    private final ShopSubscriptionStatus status;
    private final boolean demoMode;
    private final boolean demoMultiLocation;
    private final LocalDate trialStartDate;
    private final LocalDate trialEndDate;
    private final LocalDate currentPeriodStart;
    private final LocalDate currentPeriodEnd;
    private final boolean cancelAtPeriodEnd;
    private final LocalDateTime cancelledAt;
    private final LocalDate gracePeriodEndsAt;
    private final String notes;
    private final String externalCustomerId;
    private final String externalSubscriptionId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ShopSubscriptionResponse(Shop shop, ShopSubscription subscription) {
        this.configured = subscription != null;
        this.id = subscription == null ? null : subscription.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.shopActive = shop == null || shop.isActive();
        this.shopPlan = shop == null ? SubscriptionPlan.BASIC : shop.getSubscriptionPlan();
        this.multiLocationAllowed = shop != null && shop.hasMultiLocationAccess();
        this.ownerName = shop == null ? null : shop.getOwnerAdminName();
        this.ownerEmail = shop == null ? null : shop.getOwnerAdminEmail();
        this.planName = subscription == null ? null : subscription.getPlanName();
        this.billingCycle = subscription == null ? null : subscription.getBillingCycle();
        this.priceCents = subscription == null ? null : subscription.getPriceCents();
        this.currency = subscription == null ? "CAD" : subscription.getCurrency();
        this.taxRate = subscription == null ? null : subscription.getTaxRate();
        this.taxCents = subscription == null ? null : subscription.getTaxCents();
        this.totalCents = subscription == null ? null : subscription.getTotalCents();
        this.status = subscription == null ? null : subscription.getStatus();
        this.demoMode = subscription != null && subscription.isDemoMode();
        this.demoMultiLocation = subscription != null && subscription.isDemoMultiLocation();
        this.trialStartDate = subscription == null ? null : subscription.getTrialStartDate();
        this.trialEndDate = subscription == null ? null : subscription.getTrialEndDate();
        this.currentPeriodStart = subscription == null ? null : subscription.getCurrentPeriodStart();
        this.currentPeriodEnd = subscription == null ? null : subscription.getCurrentPeriodEnd();
        this.cancelAtPeriodEnd = subscription != null && subscription.isCancelAtPeriodEnd();
        this.cancelledAt = subscription == null ? null : subscription.getCancelledAt();
        this.gracePeriodEndsAt = subscription == null ? null : subscription.getGracePeriodEndsAt();
        this.notes = subscription == null ? null : subscription.getNotes();
        this.externalCustomerId = subscription == null ? null : subscription.getExternalCustomerId();
        this.externalSubscriptionId = subscription == null ? null : subscription.getExternalSubscriptionId();
        this.createdAt = subscription == null ? null : subscription.getCreatedAt();
        this.updatedAt = subscription == null ? null : subscription.getUpdatedAt();
    }

    public boolean isConfigured() { return configured; }
    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public boolean isShopActive() { return shopActive; }
    public SubscriptionPlan getShopPlan() { return shopPlan; }
    public boolean isMultiLocationAllowed() { return multiLocationAllowed; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getPlanName() { return planName; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public Long getPriceCents() { return priceCents; }
    public String getCurrency() { return currency; }
    public BigDecimal getTaxRate() { return taxRate; }
    public Long getTaxCents() { return taxCents; }
    public Long getTotalCents() { return totalCents; }
    public ShopSubscriptionStatus getStatus() { return status; }
    public boolean isDemoMode() { return demoMode; }
    public boolean isDemoMultiLocation() { return demoMultiLocation; }
    public LocalDate getTrialStartDate() { return trialStartDate; }
    public LocalDate getTrialEndDate() { return trialEndDate; }
    public LocalDate getCurrentPeriodStart() { return currentPeriodStart; }
    public LocalDate getCurrentPeriodEnd() { return currentPeriodEnd; }
    public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDate getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public String getNotes() { return notes; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public String getExternalSubscriptionId() { return externalSubscriptionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
