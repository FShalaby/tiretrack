package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollShiftSignup;

public interface PayrollShiftSignupRepository extends JpaRepository<PayrollShiftSignup, Long> {
    List<PayrollShiftSignup> findBySlot_Id(Long slotId);
    List<PayrollShiftSignup> findBySlot_PayrollPeriod_Id(Long payrollPeriodId);
    Optional<PayrollShiftSignup> findBySlot_IdAndEmployee_Id(Long slotId, Long employeeId);
    long countBySlot_Id(Long slotId);
    boolean existsBySlot_IdAndEmployee_Id(Long slotId, Long employeeId);
    void deleteBySlot_Id(Long slotId);
}
