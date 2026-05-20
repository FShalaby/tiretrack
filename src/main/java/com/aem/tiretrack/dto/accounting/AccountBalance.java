package com.aem.tiretrack.dto.accounting;

import java.math.BigDecimal;

import com.aem.tiretrack.enums.AccountType;

public class AccountBalance {
    private Long accountId;
    private String code;
    private String name;
    private AccountType type;
    private BigDecimal debits;
    private BigDecimal credits;
    private BigDecimal balance;

    public AccountBalance(Long accountId, String code, String name, AccountType type, BigDecimal debits, BigDecimal credits, BigDecimal balance) {
        this.accountId = accountId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.debits = debits;
        this.credits = credits;
        this.balance = balance;
    }

    public Long getAccountId() { return accountId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getType() { return type; }
    public BigDecimal getDebits() { return debits; }
    public BigDecimal getCredits() { return credits; }
    public BigDecimal getBalance() { return balance; }
}
