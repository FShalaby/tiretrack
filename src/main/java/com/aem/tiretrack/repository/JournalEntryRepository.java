package com.aem.tiretrack.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aem.tiretrack.model.JournalEntry;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    boolean existsByReferenceTypeAndReferenceIdAndSource(String referenceType, Long referenceId, String source);
    List<JournalEntry> findByEntryDateBetweenOrderByEntryDateDescIdDesc(LocalDate start, LocalDate end);
    List<JournalEntry> findTop25ByOrderByEntryDateDescIdDesc();
}
