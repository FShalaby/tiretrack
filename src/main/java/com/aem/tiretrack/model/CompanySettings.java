package com.aem.tiretrack.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "company_settings")
public class CompanySettings {
    @Id
    private Long id = 1L;

    @NotBlank(message = "Shop name is required")
    @Column(name = "shop_name")
    private String shopName = "Your Shop Name";

    @Column(name = "logo_url")
    private String logoUrl;

    private String phone;
    private String address;

    @Column(name = "tax_rate")
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate = new BigDecimal("13");

    @Column(name = "invoice_terms", length = 1000)
    private String invoiceTerms = "Payment is due upon receipt. Thank you for your business.";

    @PrePersist
    protected void onCreate() {
        id = 1L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = 1L;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getInvoiceTerms() {
        return invoiceTerms;
    }

    public void setInvoiceTerms(String invoiceTerms) {
        this.invoiceTerms = invoiceTerms;
    }
}
