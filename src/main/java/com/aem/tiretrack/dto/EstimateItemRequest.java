package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.InvoiceItemType;

public class EstimateItemRequest {
    private Long tireId;
    private InvoiceItemType itemType;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;

    public Long getTireId() { return tireId; }
    public void setTireId(Long tireId) { this.tireId = tireId; }
    public InvoiceItemType getItemType() { return itemType; }
    public void setItemType(InvoiceItemType itemType) { this.itemType = itemType; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
