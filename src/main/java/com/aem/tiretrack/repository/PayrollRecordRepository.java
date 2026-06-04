package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollRecord;

public interface  PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> 
{
    @Override
    @EntityGraph(attributePaths = { "employee", "payrollPeriod", "adjustments", "adjustments.employeeLoan" })
    Optional<PayrollRecord> findById(Long id);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod", "adjustments", "adjustments.employeeLoan" })
    List<PayrollRecord> findByPayrollPeriod_Id(Long payrollPeriodId);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod", "adjustments", "adjustments.employeeLoan" })
    List<PayrollRecord> findByPayrollPeriod_Shop_IdAndPayrollPeriod_Id(Long shopId, Long payrollPeriodId);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod", "adjustments", "adjustments.employeeLoan" })
    List<PayrollRecord> findByEmployee_Id(Long employeeId);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod", "adjustments", "adjustments.employeeLoan" })
    List<PayrollRecord> findByEmployee_Shop_IdAndEmployee_Id(Long shopId, Long employeeId);

    boolean existsByPayrollPeriod_IdAndEmployee_Id(Long payrollPeriodId, Long employeeId);


}
