package com.aem.tiretrack.model;

import java.math.BigDecimal;

import com.aem.tiretrack.util.PhoneNumberUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "company_settings")
public class CompanySettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @NotBlank(message = "Shop name is required")
    @Column(name = "shop_name")
    private String shopName = "Your Shop Name";

    @Column(name = "logo_url", columnDefinition = "MEDIUMTEXT")
    private String logoUrl;

    private String phone;
    private String address;

    @Column(name = "tax_rate")
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate = new BigDecimal("13");

    @Column(name = "invoice_terms", length = 1000)
    private String invoiceTerms = "Payment is due upon receipt. Thank you for your business.";

    @Column(name = "opening_time", length = 5)
    private String openingTime = "09:00";

    @Column(name = "closing_time", length = 5)
    private String closingTime = "17:00";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
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
        this.phone = PhoneNumberUtils.formatCanadian(phone);
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

    public String getOpeningTime() {
        return openingTime == null || openingTime.isBlank() ? "09:00" : openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = normalizeTime(openingTime, "09:00");
    }

    public String getClosingTime() {
        return closingTime == null || closingTime.isBlank() ? "17:00" : closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = normalizeTime(closingTime, "17:00");
    }

    private String normalizeTime(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.length() >= 5 ? value.substring(0, 5) : value;
    }
}
