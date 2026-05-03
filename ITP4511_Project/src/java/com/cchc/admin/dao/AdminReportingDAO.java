package com.cchc.admin.dao;

import com.cchc.model.Appointment;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReportingDAO {

    public List<Appointment> searchAppointments(String dateFrom, String dateTo, String status)
            throws SQLException {

        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id AS appointment_id, u.full_name AS patient_name, c.name AS clinic_name, "
                        + "s.name AS service_name, a.appointment_date, a.appointment_time, a.status "
                        + "FROM appointments a "
                        + "JOIN users u ON a.user_id = u.id "
                        + "JOIN clinics c ON a.clinic_id = c.id "
                        + "JOIN services s ON a.service_id = s.id "
                        + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append("AND a.appointment_date >= ? ");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append("AND a.appointment_date <= ? ");
            params.add(dateTo);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time DESC LIMIT 500");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment a = new Appointment();
                    a.setAppointmentId(rs.getInt("appointment_id"));
                    a.setPatientName(rs.getString("patient_name"));
                    a.setClinicName(rs.getString("clinic_name"));
                    a.setServiceName(rs.getString("service_name"));
                    a.setAppointmentDate(rs.getString("appointment_date"));
                    a.setAppointmentTime(rs.getString("appointment_time"));
                    a.setStatus(rs.getString("status"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    /** Status counts for Chart.js (given appointment date range). */
    public Map<String, Integer> countAppointmentsByStatus(String dateFrom, String dateTo) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql =
                "SELECT status, COUNT(*) AS cnt FROM appointments WHERE appointment_date >= ? AND appointment_date <= ? "
                        + "GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateFrom);
            ps.setString(2, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        }
        return map;
    }

    public int countNoShow(String dateFrom, String dateTo) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM appointments WHERE appointment_date >= ? AND appointment_date <= ? "
                        + "AND status IN ('NO_SHOW', 'No-show')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateFrom);
            ps.setString(2, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countTotalAppointments(String dateFrom, String dateTo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date >= ? AND appointment_date <= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateFrom);
            ps.setString(2, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Sum booked seats vs total capacity on timeslots whose slot_date falls in the range.
     * Used for utilization overview (Chart.js).
     */
    public int[] sumTimeslotBookedAndCapacity(String dateFrom, String dateTo) throws SQLException {
        String sql =
                "SELECT COALESCE(SUM(t.current_booked), 0) AS booked, COALESCE(SUM(t.max_capacity), 0) AS cap "
                        + "FROM timeslots t WHERE t.slot_date >= ? AND t.slot_date <= ? AND t.is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateFrom);
            ps.setString(2, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] {rs.getInt("booked"), rs.getInt("cap")};
                }
            }
        }
        return new int[] {0, 0};
    }
}
