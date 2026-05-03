package com.cchc.admin.dao;

import com.cchc.admin.model.DashboardStats;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardDAO {

    public DashboardStats fetchDashboardStats() throws SQLException {
        DashboardStats s = new DashboardStats();

        try (Connection conn = DBConnection.getConnection()) {
            s.setTodayAppointmentCount(countTodayAppointments(conn));
            s.setWaitingQueueCount(countWaitingQueue(conn));
            s.setUtilizationPercent(computeTodayUtilization(conn));
            s.setNoShowRatePercent(computeNoShowRateLast30Days(conn));
        }

        return s;
    }

    private static int countTodayAppointments(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int countWaitingQueue(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM queue_tickets WHERE status = 'WAITING'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Slot utilization today: sum(current_booked) / sum(max_capacity) on active timeslots for today.
     */
    private static double computeTodayUtilization(Connection conn) throws SQLException {
        String sql =
                "SELECT COALESCE(SUM(current_booked), 0) AS booked, COALESCE(SUM(max_capacity), 0) AS cap "
                        + "FROM timeslots WHERE slot_date = CURDATE() AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return 0d;
            }
            int booked = rs.getInt("booked");
            int cap = rs.getInt("cap");
            if (cap <= 0) {
                return 0d;
            }
            return Math.min(100d, 100d * booked / cap);
        }
    }

    /**
     * No-show rate over the last 30 days (appointments with a date in range).
     */
    private static double computeNoShowRateLast30Days(Connection conn) throws SQLException {
        String sql =
                "SELECT "
                        + "SUM(CASE WHEN status IN ('NO_SHOW', 'No-show') THEN 1 ELSE 0 END) AS ns, "
                        + "COUNT(*) AS total "
                        + "FROM appointments "
                        + "WHERE appointment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                        + "AND appointment_date <= CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return 0d;
            }
            int total = rs.getInt("total");
            if (total <= 0) {
                return 0d;
            }
            int ns = rs.getInt("ns");
            return 100d * ns / total;
        }
    }
}
