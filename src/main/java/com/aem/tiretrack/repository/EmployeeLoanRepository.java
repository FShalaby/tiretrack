package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.enums.LoanStatus;
import com.aem.tiretrack.model.EmployeeLoan;

public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
    @Override
    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeLoan> findAll();

    @Override
    @EntityGraph(attributePaths = { "employee", "shop" })
    Optional<EmployeeLoan> findById(Long id);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeLoan> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeLoan> findByShop_IdOrderByCreatedAtDesc(Long shopId);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeLoan> findByShop_IdAndStatusOrderByCreatedAtDesc(Long shopId, LoanStatus status);

    @EntityGraph(attributePaths = { "employee", "shop" })
    List<EmployeeLoan> findByStatusOrderByCreatedAtDesc(LoanStatus status);
}
