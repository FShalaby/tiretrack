package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.InvoiceItemType;
import com.aem.tiretrack.model.InvoiceItem;

public class InvoiceItemResponse {
    private Long id;
    private Long tireId;
    private InvoiceItemType itemType;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public InvoiceItemResponse(InvoiceItem item) {
        this.id = item.getId();
        this.tireId = item.getTireId();
        this.itemType = item.getItemType();
        this.itemName = item.getItemName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.totalPrice = item.getTotalPrice();
    }

    public Long getId() { return id; }
    public Long getTireId() { return tireId; }
    public InvoiceItemType getItemType() { return itemType; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
