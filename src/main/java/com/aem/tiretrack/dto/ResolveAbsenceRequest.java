package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.AbsenceDecision;

public class ResolveAbsenceRequest {
    private AbsenceDecision decision;
    private String notes;

    public AbsenceDecision getDecision() {
        return decision;
    }

    public void setDecision(AbsenceDecision decision) {
        this.decision = decision;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
