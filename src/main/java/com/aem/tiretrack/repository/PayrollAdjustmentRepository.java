package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollAdjustment;

public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {
    @Override
    @EntityGraph(attributePaths = { "payrollRecord", "payrollRecord.employee", "payrollRecord.payrollPeriod", "employeeLoan" })
    Optional<PayrollAdjustment> findById(Long id);

    @EntityGraph(attributePaths = { "employeeLoan" })
    List<PayrollAdjustment> findByPayrollRecord_IdOrderByCreatedAtAsc(Long payrollRecordId);
}
