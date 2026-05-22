package com.aem.tiretrack.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.WorkShift;


public interface  WorkShiftRepository extends JpaRepository<WorkShift, Long>
{
    List<WorkShift> findByEmployee_Id(Long employeeId);   
    List<WorkShift> findByEmployee_IdAndShiftDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<WorkShift> findByShiftDateBetween(LocalDate start, LocalDate end);

}
