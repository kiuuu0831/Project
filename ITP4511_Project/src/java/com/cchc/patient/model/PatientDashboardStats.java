package com.cchc.patient.model;

/** Chart + summary data for patient dashboard (request-scoped JavaBean). */
public class PatientDashboardStats {
    private int[] monthCounts;
    private String[] monthLabels;
    private int upcomingCount;
    private int activeQueueCount;

    public int[] getMonthCounts() {
        return monthCounts;
    }

    public void setMonthCounts(int[] monthCounts) {
        this.monthCounts = monthCounts;
    }

    public String[] getMonthLabels() {
        return monthLabels;
    }

    public void setMonthLabels(String[] monthLabels) {
        this.monthLabels = monthLabels;
    }

    public int getUpcomingCount() {
        return upcomingCount;
    }

    public void setUpcomingCount(int upcomingCount) {
        this.upcomingCount = upcomingCount;
    }

    public int getActiveQueueCount() {
        return activeQueueCount;
    }

    public void setActiveQueueCount(int activeQueueCount) {
        this.activeQueueCount = activeQueueCount;
    }
}
