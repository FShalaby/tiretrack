package com.aem.tiretrack.dto;

import com.aem.tiretrack.enums.TireRequestStatus;

public class TireRequestStatusUpdateRequest {
    private TireRequestStatus status;
    private String adminResponse;

    public TireRequestStatus getStatus() { return status; }
    public void setStatus(TireRequestStatus status) { this.status = status; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
}
