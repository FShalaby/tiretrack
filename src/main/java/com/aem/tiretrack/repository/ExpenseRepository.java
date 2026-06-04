package com.aem.tiretrack.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate start, LocalDate end);
    List<Expense> findTop25ByOrderByExpenseDateDescIdDesc();
    List<Expense> findTop25ByShop_IdOrderByExpenseDateDescIdDesc(Long shopId);
}
