package com.aem.tiretrack.dto.accounting;

import java.math.BigDecimal;
import java.util.List;

import com.aem.tiretrack.model.Expense;
import com.aem.tiretrack.model.JournalEntry;
import com.aem.tiretrack.model.Vendor;

public class AccountingReport {
    private List<AccountBalance> trialBalance;
    private List<AccountBalance> profitAndLoss;
    private List<AccountBalance> balanceSheet;
    private List<Expense> recentExpenses;
    private List<JournalEntry> recentJournalEntries;
    private List<Vendor> vendors;
    private BigDecimal revenue;
    private BigDecimal expenses;
    private BigDecimal netIncome;
    private BigDecimal assets;
    private BigDecimal liabilities;
    private BigDecimal equity;

    public AccountingReport(List<AccountBalance> trialBalance, List<AccountBalance> profitAndLoss, List<AccountBalance> balanceSheet, List<Expense> recentExpenses, List<JournalEntry> recentJournalEntries, List<Vendor> vendors, BigDecimal revenue, BigDecimal expenses, BigDecimal netIncome, BigDecimal assets, BigDecimal liabilities, BigDecimal equity) {
        this.trialBalance = trialBalance;
        this.profitAndLoss = profitAndLoss;
        this.balanceSheet = balanceSheet;
        this.recentExpenses = recentExpenses;
        this.recentJournalEntries = recentJournalEntries;
        this.vendors = vendors;
        this.revenue = revenue;
        this.expenses = expenses;
        this.netIncome = netIncome;
        this.assets = assets;
        this.liabilities = liabilities;
        this.equity = equity;
    }

    public List<AccountBalance> getTrialBalance() { return trialBalance; }
    public List<AccountBalance> getProfitAndLoss() { return profitAndLoss; }
    public List<AccountBalance> getBalanceSheet() { return balanceSheet; }
    public List<Expense> getRecentExpenses() { return recentExpenses; }
    public List<JournalEntry> getRecentJournalEntries() { return recentJournalEntries; }
    public List<Vendor> getVendors() { return vendors; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getExpenses() { return expenses; }
    public BigDecimal getNetIncome() { return netIncome; }
    public BigDecimal getAssets() { return assets; }
    public BigDecimal getLiabilities() { return liabilities; }
    public BigDecimal getEquity() { return equity; }
}
