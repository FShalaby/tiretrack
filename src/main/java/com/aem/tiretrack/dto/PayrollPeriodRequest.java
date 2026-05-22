package com.aem.tiretrack.dto;

import java.time.LocalDate;

public class PayrollPeriodRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    public PayrollPeriodRequest() {
    }

    public PayrollPeriodRequest(LocalDate startDate, LocalDate endDate, String notes) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
