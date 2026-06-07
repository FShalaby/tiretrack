package com.aem.tiretrack.dto.accounting;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.VendorCategory;
import com.aem.tiretrack.model.Vendor;

public class VendorResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String category;
    private VendorCategory categoryKey;
    private String customCategory;
    private String notes;
    private Long shopId;
    private String shopName;
    private String createdBy;
    private LocalDateTime createdAt;

    public VendorResponse(Vendor vendor) {
        this.id = vendor.getId();
        this.name = vendor.getName();
        this.email = vendor.getEmail();
        this.phone = vendor.getPhone();
        this.category = vendor.getCategory();
        this.categoryKey = vendor.getCategoryKey();
        this.customCategory = vendor.getCustomCategory();
        this.notes = vendor.getNotes();
        this.shopId = vendor.getShopId();
        this.shopName = vendor.getShopName();
        this.createdBy = vendor.getCreatedBy();
        this.createdAt = vendor.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCategory() { return category; }
    public VendorCategory getCategoryKey() { return categoryKey; }
    public String getCustomCategory() { return customCategory; }
    public String getNotes() { return notes; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
