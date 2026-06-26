package com.aem.tiretrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopPaymentMethod;
import com.aem.tiretrack.enums.ShopPaymentStatus;

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
@Table(name = "shop_payments")
public class ShopPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private ShopSubscription subscription;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents = 0L;

    @Column(nullable = false)
    private String currency = "CAD";

    @Column(name = "tax_cents", nullable = false)
    private Long taxCents = 0L;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private ShopPaymentMethod paymentMethod = ShopPaymentMethod.E_TRANSFER;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private ShopPaymentStatus paymentStatus = ShopPaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_admin_id")
    private User recordedByAdmin;

    @Column(name = "external_invoice_id")
    private String externalInvoiceId;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

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
        if (amountCents == null) {
            amountCents = 0L;
        }
        if (currency == null || currency.isBlank()) {
            currency = "CAD";
        }
        if (taxCents == null) {
            taxCents = 0L;
        }
        if (totalCents == null) {
            totalCents = amountCents + taxCents;
        }
        if (paymentMethod == null) {
            paymentMethod = ShopPaymentMethod.E_TRANSFER;
        }
        if (paymentStatus == null) {
            paymentStatus = ShopPaymentStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public ShopSubscription getSubscription() { return subscription; }
    public void setSubscription(ShopSubscription subscription) { this.subscription = subscription; }
    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getTaxCents() { return taxCents; }
    public void setTaxCents(Long taxCents) { this.taxCents = taxCents; }
    public Long getTotalCents() { return totalCents; }
    public void setTotalCents(Long totalCents) { this.totalCents = totalCents; }
    public ShopPaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(ShopPaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public ShopPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(ShopPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getRecordedByAdmin() { return recordedByAdmin; }
    public void setRecordedByAdmin(User recordedByAdmin) { this.recordedByAdmin = recordedByAdmin; }
    public String getExternalInvoiceId() { return externalInvoiceId; }
    public void setExternalInvoiceId(String externalInvoiceId) { this.externalInvoiceId = externalInvoiceId; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
