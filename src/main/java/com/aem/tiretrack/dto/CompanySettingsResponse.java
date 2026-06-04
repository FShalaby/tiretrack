package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import com.aem.tiretrack.model.CompanySettings;
import com.aem.tiretrack.model.Shop;

public class CompanySettingsResponse {
    private final Long id;
    private final Long shopId;
    private final String shopTenantName;
    private final String shopName;
    private final String logoUrl;
    private final String phone;
    private final String address;
    private final BigDecimal taxRate;
    private final String invoiceTerms;

    public CompanySettingsResponse(CompanySettings settings) {
        Shop shop = settings.getShop();
        this.id = settings.getId();
        this.shopId = shop == null ? null : shop.getId();
        this.shopTenantName = shop == null ? null : shop.getName();
        this.shopName = settings.getShopName();
        this.logoUrl = settings.getLogoUrl();
        this.phone = settings.getPhone();
        this.address = settings.getAddress();
        this.taxRate = settings.getTaxRate();
        this.invoiceTerms = settings.getInvoiceTerms();
    }

    public Long getId() { return id; }
    public Long getShopId() { return shopId; }
    public String getShopTenantName() { return shopTenantName; }
    public String getShopName() { return shopName; }
    public String getLogoUrl() { return logoUrl; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public BigDecimal getTaxRate() { return taxRate; }
    public String getInvoiceTerms() { return invoiceTerms; }
}
