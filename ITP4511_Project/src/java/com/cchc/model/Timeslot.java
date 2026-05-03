package com.cchc.model;

public class Timeslot {
    private int id;
    private int clinicId;
    private int serviceId;
    private String slotDate;
    private String startTime;
    private String endTime;
    private int maxCapacity;
    private int currentBooked;
    private boolean active = true;
    private String clinicNameDisplay;
    private String serviceNameDisplay;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClinicId() {
        return clinicId;
    }

    public void setClinicId(int clinicId) {
        this.clinicId = clinicId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(String slotDate) {
        this.slotDate = slotDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentBooked() {
        return currentBooked;
    }

    public void setCurrentBooked(int currentBooked) {
        this.currentBooked = currentBooked;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getClinicNameDisplay() {
        return clinicNameDisplay;
    }

    public void setClinicNameDisplay(String clinicNameDisplay) {
        this.clinicNameDisplay = clinicNameDisplay;
    }

    public String getServiceNameDisplay() {
        return serviceNameDisplay;
    }

    public void setServiceNameDisplay(String serviceNameDisplay) {
        this.serviceNameDisplay = serviceNameDisplay;
    }
}
