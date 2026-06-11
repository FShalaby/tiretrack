package com.aem.tiretrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AccountingPaymentMethod;
import com.aem.tiretrack.enums.ExpenseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String vendor;

    @Column(name = "vendor_id")
    private Long vendorId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", insertable = false, updatable = false)
    private Vendor vendorRecord;

    private String category = "Operating";

    @Enumerated(EnumType.STRING)
    @Column(name = "category_key")
    private ExpenseCategory categoryKey = ExpenseCategory.SUPPLIES;

    @Column(name = "custom_category")
    private String customCategory;

    @Column(name = "expense_account_id")
    private Long expenseAccountId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_account_id", insertable = false, updatable = false)
    private AccountingAccount expenseAccount;

    @Column(name = "expense_date")
    private LocalDate expenseDate = LocalDate.now();

    @Column(name = "due_date")
    private LocalDate dueDate;

    @DecimalMin("0.0")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    @DecimalMin("0.0")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin("0.0")
    private BigDecimal total = BigDecimal.ZERO;

    private String status = "PAID";

    @Column(name = "payment_method")
    private String paymentMethod = "Cash";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_key")
    private AccountingPaymentMethod paymentMethodKey = AccountingPaymentMethod.CASH;

    @Column(name = "custom_payment_method")
    private String customPaymentMethod;

    private String notes;

    @Column(name = "admin_user_id")
    @JsonIgnore
    private Long adminUserId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ShopLocation shopLocation;

    @Transient
    private Long requestedLocationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private User adminUser;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            total = subtotal.add(taxAmount);
        }
    }

    public Long getId() { return id; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ExpenseCategory getCategoryKey() { return categoryKey; }
    public void setCategoryKey(ExpenseCategory categoryKey) { this.categoryKey = categoryKey; }
    public String getCustomCategory() { return customCategory; }
    public void setCustomCategory(String customCategory) { this.customCategory = customCategory; }
    public Long getExpenseAccountId() { return expenseAccountId; }
    public void setExpenseAccountId(Long expenseAccountId) { this.expenseAccountId = expenseAccountId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public AccountingPaymentMethod getPaymentMethodKey() { return paymentMethodKey; }
    public void setPaymentMethodKey(AccountingPaymentMethod paymentMethodKey) { this.paymentMethodKey = paymentMethodKey; }
    public String getCustomPaymentMethod() { return customPaymentMethod; }
    public void setCustomPaymentMethod(String customPaymentMethod) { this.customPaymentMethod = customPaymentMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }
    public Long getShopId() { return shop == null ? null : shop.getId(); }
    public String getShopName() { return shop == null ? null : shop.getName(); }
    public ShopLocation getShopLocation() { return shopLocation; }
    public void setShopLocation(ShopLocation shopLocation) { this.shopLocation = shopLocation; }
    public Long getLocationId() { return shopLocation == null ? null : shopLocation.getId(); }
    public void setLocationId(Long locationId) { this.requestedLocationId = locationId; }
    public Long getRequestedLocationId() { return requestedLocationId; }
    public String getLocationName() { return shopLocation == null ? null : shopLocation.getName(); }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
