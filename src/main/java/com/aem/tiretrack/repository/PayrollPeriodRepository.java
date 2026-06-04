package com.aem.tiretrack.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.PayrollPeriod;

public interface  PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> 
{
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
    LocalDate endDate,
    LocalDate startDate
    );

    boolean existsByShop_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
    Long shopId,
    LocalDate endDate,
    LocalDate startDate
    );

    List<PayrollPeriod> findByShop_Id(Long shopId);

}
