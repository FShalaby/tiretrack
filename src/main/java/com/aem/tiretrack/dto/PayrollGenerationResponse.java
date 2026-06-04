package com.aem.tiretrack.dto;

import java.util.List;

import com.aem.tiretrack.model.PayrollRecord;

public class PayrollGenerationResponse {
    private final List<PayrollRecordResponse> records;
    private final List<String> skippedReasons;
    private final String message;

    public PayrollGenerationResponse(List<PayrollRecord> records, List<String> skippedReasons) {
        this.records = records.stream().map(PayrollRecordResponse::new).toList();
        this.skippedReasons = skippedReasons;
        this.message = buildMessage(this.records.size(), skippedReasons.size());
    }

    public List<PayrollRecordResponse> getRecords() {
        return records;
    }

    public List<String> getSkippedReasons() {
        return skippedReasons;
    }

    public String getMessage() {
        return message;
    }

    private String buildMessage(int createdCount, int skippedCount) {
        if (createdCount == 0 && skippedCount == 0) {
            return "No eligible payroll hours were found for this period.";
        }

        if (createdCount == 0) {
            return "No payroll records were generated. Review the skipped employee reasons.";
        }

        return "Generated " + createdCount + " payroll record" + (createdCount == 1 ? "" : "s")
                + (skippedCount > 0 ? " and skipped " + skippedCount + "." : ".");
    }
}
