package com.aem.tiretrack.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.WorkShift;


public interface  WorkShiftRepository extends JpaRepository<WorkShift, Long>
{
    @Override
    @EntityGraph(attributePaths = "employee")
    List<WorkShift> findAll();

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<WorkShift> findById(Long id);

    @EntityGraph(attributePaths = "employee")
    List<WorkShift> findByEmployee_Id(Long employeeId);   

    @EntityGraph(attributePaths = "employee")
    List<WorkShift> findByEmployee_IdAndShiftDateBetween(Long employeeId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = "employee")
    List<WorkShift> findByShiftDateBetween(LocalDate start, LocalDate end);

}
