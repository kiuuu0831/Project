package com.cchc.util;

/**
 * Maps staff UI labels to {@code queue_tickets.status} ENUM values:
 * WAITING, CALLED, COMPLETED, SKIPPED, EXPIRED.
 */
public final class QueueStatusUtil {

    private QueueStatusUtil() {
    }

    public static String toCanonical(String formValue) {
        if (formValue == null) {
            return null;
        }
        String t = formValue.trim();
        if (t.isEmpty()) {
            return null;
        }
        return switch (t) {
            case "Called", "CALLED" -> "CALLED";
            case "Skipped", "SKIPPED" -> "SKIPPED";
            case "Served", "COMPLETED" -> "COMPLETED";
            case "Waiting", "WAITING" -> "WAITING";
            case "EXPIRED" -> "EXPIRED";
            default -> formValue.trim();
        };
    }
}
