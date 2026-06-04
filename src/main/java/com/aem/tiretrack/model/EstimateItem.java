package com.aem.tiretrack.model;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.InvoiceItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "estimate_items")
public class EstimateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @Column(name = "tire_id")
    private Long tireId;

    @NotNull(message = "Item type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private InvoiceItemType itemType = InvoiceItemType.SERVICE;

    @Column(name = "item_name")
    private String itemName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;

    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    @Column(name = "unit_price")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Line total cannot be negative")
    @Column(name = "line_total")
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public Long getId() { return id; }
    public Estimate getEstimate() { return estimate; }
    public void setEstimate(Estimate estimate) { this.estimate = estimate; }
    public Long getTireId() { return tireId; }
    public void setTireId(Long tireId) { this.tireId = tireId; }
    public InvoiceItemType getItemType() { return itemType; }
    public void setItemType(InvoiceItemType itemType) { this.itemType = itemType; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
