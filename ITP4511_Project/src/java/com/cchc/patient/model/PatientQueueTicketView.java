package com.cchc.patient.model;

public class PatientQueueTicketView {
    private int id;
    private int queueNumber;
    private String clinicName;
    private String serviceName;
    private String status;
    private String joinedAt;
    private Integer estimatedWait;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getEstimatedWait() {
        return estimatedWait;
    }

    public void setEstimatedWait(Integer estimatedWait) {
        this.estimatedWait = estimatedWait;
    }
}
