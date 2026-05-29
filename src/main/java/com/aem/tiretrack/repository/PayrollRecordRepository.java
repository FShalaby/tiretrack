package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollRecord;

public interface  PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> 
{
    @Override
    @EntityGraph(attributePaths = { "employee", "payrollPeriod" })
    Optional<PayrollRecord> findById(Long id);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod" })
    List<PayrollRecord> findByPayrollPeriod_Id(Long payrollPeriodId);

    @EntityGraph(attributePaths = { "employee", "payrollPeriod" })
    List<PayrollRecord> findByEmployee_Id(Long employeeId);

    boolean existsByPayrollPeriod_IdAndEmployee_Id(Long payrollPeriodId, Long employeeId);


}
