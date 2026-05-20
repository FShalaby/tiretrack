package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.AccountingAccount;

public interface AccountingAccountRepository extends JpaRepository<AccountingAccount, Long> {
    Optional<AccountingAccount> findByCode(String code);
    boolean existsByCode(String code);
    List<AccountingAccount> findByActiveTrueOrderByCodeAsc();
}
