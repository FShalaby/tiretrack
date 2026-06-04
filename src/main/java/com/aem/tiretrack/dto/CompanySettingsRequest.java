package com.aem.tiretrack.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class CompanySettingsRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;
    private String logoUrl;
    private String phone;
    private String address;
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate;
    private String invoiceTerms;

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public String getInvoiceTerms() { return invoiceTerms; }
    public void setInvoiceTerms(String invoiceTerms) { this.invoiceTerms = invoiceTerms; }
}
