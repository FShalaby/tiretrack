package com.aem.tiretrack.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollPeriod;

public interface  PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> 
{
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
    LocalDate endDate,
    LocalDate startDate
    );

}
