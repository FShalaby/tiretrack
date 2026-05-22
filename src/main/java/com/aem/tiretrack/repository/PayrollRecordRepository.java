package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollRecord;

public interface  PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> 
{
    List<PayrollRecord> findByPayrollPeriod_Id(Long payrollPeriodId);

    List<PayrollRecord> findByEmployee_Id(Long employeeId);

    boolean existsByPayrollPeriod_IdAndEmployee_Id(Long payrollPeriodId, Long employeeId);


}
