package com.cchc.admin.model;

public class DashboardStats {

    private int todayAppointmentCount;
    private int waitingQueueCount;
    private double utilizationPercent;
    private double noShowRatePercent;

    public int getTodayAppointmentCount() {
        return todayAppointmentCount;
    }

    public void setTodayAppointmentCount(int todayAppointmentCount) {
        this.todayAppointmentCount = todayAppointmentCount;
    }

    public int getWaitingQueueCount() {
        return waitingQueueCount;
    }

    public void setWaitingQueueCount(int waitingQueueCount) {
        this.waitingQueueCount = waitingQueueCount;
    }

    public double getUtilizationPercent() {
        return utilizationPercent;
    }

    public void setUtilizationPercent(double utilizationPercent) {
        this.utilizationPercent = utilizationPercent;
    }

    public double getNoShowRatePercent() {
        return noShowRatePercent;
    }

    public void setNoShowRatePercent(double noShowRatePercent) {
        this.noShowRatePercent = noShowRatePercent;
    }
}
