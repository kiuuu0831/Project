package com.cchc.util;

/**
 * Maps staff UI values to {@code appointments.status} ENUM:
 * PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW (cchc_admin.sql).
 */
public final class AppointmentStatusUtil {

    private AppointmentStatusUtil() {
    }

    /** Accepts ENUM literals or legacy display labels from old forms. */
    public static String toCanonical(String staffFormValue) {
        if (staffFormValue == null) {
            return null;
        }
        String v = staffFormValue.trim();
        if (v.isEmpty()) {
            return null;
        }
        return switch (v) {
            case "No-show" -> "NO_SHOW";
            case "Cancelled by clinic" -> "CANCELLED";
            case "Arrived" -> "CONFIRMED";
            case "Booked" -> "PENDING";
            case "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW" -> v;
            default -> v;
        };
    }
}
