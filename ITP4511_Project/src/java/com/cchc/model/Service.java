package com.cchc.model;

public class Service {
    private int id;
    private int clinicId;
    private String name;
    private String description;
    private int durationMinutes = 30;
    private boolean active = true;
    private String clinicNameDisplay;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
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
}
