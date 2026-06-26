package com.aem.tiretrack.dto.billing;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopPaymentMethod;
import com.aem.tiretrack.enums.ShopPaymentStatus;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopPayment;
import com.aem.tiretrack.model.User;

public class ShopPaymentResponse {
    private final Long id;
    private final Long shopId;
    private final String shopName;
    private final Long subscriptionId;
    private final Long amountCents;
    private final String currency;
    private final Long taxCents;
    private final Long totalCents;
    private final ShopPaymentMethod paymentMethod;
    private final ShopPaymentStatus paymentStatus;
    private final LocalDateTime paidAt;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final String referenceNumber;
    private final String invoiceNumber;
    private final String notes;
    private final Long recordedByAdminId;
    private final String recordedByAdminName;
    private final String externalInvoiceId;
    private final String externalPaymentId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ShopPaymentResponse(ShopPayment payment) {
        Shop shop = payment.getShop();
        User recordedBy = payment.getRecordedByAdmin();
        this.id = payment.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopName = shop == null ? null : shop.getName();
        this.subscriptionId = payment.getSubscription() == null ? null : payment.getSubscription().getId();
        this.amountCents = payment.getAmountCents();
        this.currency = payment.getCurrency();
        this.taxCents = payment.getTaxCents();
        this.totalCents = payment.getTotalCents();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentStatus = payment.getPaymentStatus();
        this.paidAt = payment.getPaidAt();
        this.periodStart = payment.getPeriodStart();
        this.periodEnd = payment.getPeriodEnd();
        this.referenceNumber = payment.getReferenceNumber();
        this.invoiceNumber = payment.getInvoiceNumber();
        this.notes = payment.getNotes();
        this.recordedByAdminId = recordedBy == null ? null : recordedBy.getId();
        this.recordedByAdminName = recordedBy == null ? null : recordedBy.getFullName();
        this.externalInvoiceId = payment.getExternalInvoiceId();
        this.externalPaymentId = payment.getExternalPaymentId();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getSubscriptionId() { return subscriptionId; }
    public Long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public Long getTaxCents() { return taxCents; }
    public Long getTotalCents() { return totalCents; }
    public ShopPaymentMethod getPaymentMethod() { return paymentMethod; }
    public ShopPaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public String getReferenceNumber() { return referenceNumber; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getNotes() { return notes; }
    public Long getRecordedByAdminId() { return recordedByAdminId; }
    public String getRecordedByAdminName() { return recordedByAdminName; }
    public String getExternalInvoiceId() { return externalInvoiceId; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
