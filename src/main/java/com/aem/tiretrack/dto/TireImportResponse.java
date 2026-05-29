package com.aem.tiretrack.dto;

import java.util.List;

public class TireImportResponse {
    private final int totalRows;
    private final int createdCount;
    private final int updatedCount;
    private final int skippedCount;
    private final List<String> errors;

    public TireImportResponse(int totalRows, int createdCount, int updatedCount, int skippedCount, List<String> errors) {
        this.totalRows = totalRows;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.skippedCount = skippedCount;
        this.errors = errors;
    }

    public int getTotalRows() { return totalRows; }
    public int getCreatedCount() { return createdCount; }
    public int getUpdatedCount() { return updatedCount; }
    public int getSkippedCount() { return skippedCount; }
    public List<String> getErrors() { return errors; }
}
