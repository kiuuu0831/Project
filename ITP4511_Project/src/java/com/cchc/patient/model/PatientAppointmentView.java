package com.cchc.patient.model;

import com.cchc.model.Timeslot;
import java.util.List;

/** Patient-facing appointment row for JSP / MVC. */
public class PatientAppointmentView {
    private int id;
    private int clinicId;
    private int serviceId;
    private Integer timeslotId;
    private String clinicName;
    private String serviceName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private boolean canModify;
    private List<Timeslot> rescheduleOptions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(Integer timeslotId) {
        this.timeslotId = timeslotId;
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

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCanModify() {
        return canModify;
    }

    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
    }

    public List<Timeslot> getRescheduleOptions() {
        return rescheduleOptions;
    }

    public void setRescheduleOptions(List<Timeslot> rescheduleOptions) {
        this.rescheduleOptions = rescheduleOptions;
    }
}
