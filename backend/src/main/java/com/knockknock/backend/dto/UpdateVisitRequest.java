package com.knockknock.backend.dto;

public class UpdateVisitRequest {
    private String unitNumber;
    private String purpose;
    private String visitDate;

    public UpdateVisitRequest() {}

    public UpdateVisitRequest(String unitNumber, String purpose, String visitDate) {
        this.unitNumber = unitNumber;
        this.purpose = purpose;
        this.visitDate = visitDate;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }
}
