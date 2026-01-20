package com.vet.vet_scheduler_service;

public class AppointmentRequest {
    private String owner;
    private String petName;
    private String visitDate;
    private Integer requestId;

    // Gettery i Settery (wymagane przez Jacksona)
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
}