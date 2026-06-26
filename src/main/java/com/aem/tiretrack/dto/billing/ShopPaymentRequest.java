package com.aem.tiretrack.dto.billing;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopPaymentMethod;
import com.aem.tiretrack.enums.ShopPaymentStatus;

public class ShopPaymentRequest {
    private Long amountCents;
    private String currency;
    private Long taxCents;
    private Long totalCents;
    private ShopPaymentMethod paymentMethod;
    private ShopPaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String referenceNumber;
    private String invoiceNumber;
    private String notes;
    private String externalInvoiceId;
    private String externalPaymentId;

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
    public String getExternalInvoiceId() { return externalInvoiceId; }
    public void setExternalInvoiceId(String externalInvoiceId) { this.externalInvoiceId = externalInvoiceId; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }
}
