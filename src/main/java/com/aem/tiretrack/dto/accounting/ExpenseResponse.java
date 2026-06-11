package com.aem.tiretrack.dto.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AccountingPaymentMethod;
import com.aem.tiretrack.enums.ExpenseCategory;
import com.aem.tiretrack.model.Expense;

public class ExpenseResponse {
    private Long id;
    private String vendor;
    private Long vendorId;
    private String category;
    private ExpenseCategory categoryKey;
    private String customCategory;
    private Long expenseAccountId;
    private LocalDate expenseDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String status;
    private String paymentMethod;
    private AccountingPaymentMethod paymentMethodKey;
    private String customPaymentMethod;
    private String notes;
    private Long shopId;
    private String shopName;
    private Long locationId;
    private String locationName;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public ExpenseResponse(Expense expense) {
        this.id = expense.getId();
        this.vendor = expense.getVendor();
        this.vendorId = expense.getVendorId();
        this.category = expense.getCategory();
        this.categoryKey = expense.getCategoryKey();
        this.customCategory = expense.getCustomCategory();
        this.expenseAccountId = expense.getExpenseAccountId();
        this.expenseDate = expense.getExpenseDate();
        this.dueDate = expense.getDueDate();
        this.subtotal = expense.getSubtotal();
        this.taxAmount = expense.getTaxAmount();
        this.total = expense.getTotal();
        this.status = expense.getStatus();
        this.paymentMethod = expense.getPaymentMethod();
        this.paymentMethodKey = expense.getPaymentMethodKey();
        this.customPaymentMethod = expense.getCustomPaymentMethod();
        this.notes = expense.getNotes();
        this.shopId = expense.getShopId();
        this.shopName = expense.getShopName();
        this.locationId = expense.getLocationId();
        this.locationName = expense.getLocationName();
        this.createdBy = expense.getCreatedBy();
        this.createdAt = expense.getCreatedAt();
        this.paidAt = expense.getPaidAt();
    }

    public Long getId() { return id; }
    public String getVendor() { return vendor; }
    public Long getVendorId() { return vendorId; }
    public String getCategory() { return category; }
    public ExpenseCategory getCategoryKey() { return categoryKey; }
    public String getCustomCategory() { return customCategory; }
    public Long getExpenseAccountId() { return expenseAccountId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public AccountingPaymentMethod getPaymentMethodKey() { return paymentMethodKey; }
    public String getCustomPaymentMethod() { return customPaymentMethod; }
    public String getNotes() { return notes; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
