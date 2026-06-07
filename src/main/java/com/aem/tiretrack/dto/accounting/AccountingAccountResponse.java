package com.aem.tiretrack.dto.accounting;

import java.time.LocalDateTime;

import com.aem.tiretrack.enums.AccountType;
import com.aem.tiretrack.model.AccountingAccount;

public class AccountingAccountResponse {
    private Long id;
    private String code;
    private String name;
    private AccountType type;
    private boolean systemAccount;
    private Long shopId;
    private String shopName;
    private String createdBy;
    private boolean active;
    private LocalDateTime createdAt;

    public AccountingAccountResponse(AccountingAccount account) {
        this.id = account.getId();
        this.code = account.getCode();
        this.name = account.getName();
        this.type = account.getType();
        this.systemAccount = account.isSystemAccount();
        this.shopId = account.getShopId();
        this.shopName = account.getShopName();
        this.createdBy = account.getCreatedBy();
        this.active = account.isActive();
        this.createdAt = account.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getType() { return type; }
    public boolean isSystemAccount() { return systemAccount; }
    public Long getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getCreatedBy() { return createdBy; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
