package com.aem.tiretrack.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "journal_entry_lines")
public class JournalEntryLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountingAccount account;

    @Column(nullable = false)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal credit = BigDecimal.ZERO;

    private String memo;

    public Long getId() { return id; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
    public AccountingAccount getAccount() { return account; }
    public void setAccount(AccountingAccount account) { this.account = account; }
    public BigDecimal getDebit() { return debit == null ? BigDecimal.ZERO : debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit == null ? BigDecimal.ZERO : debit; }
    public BigDecimal getCredit() { return credit == null ? BigDecimal.ZERO : credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit == null ? BigDecimal.ZERO : credit; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
