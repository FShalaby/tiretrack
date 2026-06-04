package com.aem.tiretrack.dto.accounting;

import java.util.List;

public class DuplicatePaymentDiagnosticResponse {
    private final Long invoiceId;
    private final Long shopId;
    private final String shopName;
    private final int paymentEntryCount;
    private final List<Long> journalEntryIds;
    private final List<String> sources;

    public DuplicatePaymentDiagnosticResponse(
            Long invoiceId,
            Long shopId,
            String shopName,
            int paymentEntryCount,
            List<Long> journalEntryIds,
            List<String> sources) {
        this.invoiceId = invoiceId;
        this.shopId = shopId;
        this.shopName = shopName;
        this.paymentEntryCount = paymentEntryCount;
        this.journalEntryIds = journalEntryIds;
        this.sources = sources;
    }

    public Long getInvoiceId() { return invoiceId; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public int getPaymentEntryCount() { return paymentEntryCount; }
    public List<Long> getJournalEntryIds() { return journalEntryIds; }
    public List<String> getSources() { return sources; }
}
