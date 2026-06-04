package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.model.EstimateItem;

public class EstimateItemResponse {
    private final Long id;
    private final Long tireId;
    private final InvoiceItemType itemType;
    private final String itemName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    public EstimateItemResponse(EstimateItem item) {
        this.id = item.getId();
        this.tireId = item.getTireId();
        this.itemType = item.getItemType();
        this.itemName = item.getItemName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.lineTotal = item.getLineTotal();
    }

    public Long getId() { return id; }
    public Long getTireId() { return tireId; }
    public InvoiceItemType getItemType() { return itemType; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
