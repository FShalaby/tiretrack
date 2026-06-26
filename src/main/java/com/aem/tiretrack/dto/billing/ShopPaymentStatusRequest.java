package com.aem.tiretrack.dto.billing;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.ShopPaymentStatus;

public class ShopPaymentStatusRequest {
    private ShopPaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private String notes;

    public ShopPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(ShopPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
