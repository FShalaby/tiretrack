package com.aem.tiretrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollShiftSlot;

public interface PayrollShiftSlotRepository extends JpaRepository<PayrollShiftSlot, Long> {
    List<PayrollShiftSlot> findByPayrollPeriod_IdOrderByShiftDateAscStartTimeAsc(Long payrollPeriodId);
    List<PayrollShiftSlot> findAllByOrderByShiftDateAscStartTimeAsc();
}
