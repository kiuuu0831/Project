package com.cchc.dao;

import com.cchc.model.Appointment;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql =
                "SELECT a.id AS appointment_id, u.full_name AS patient_name, c.name AS clinic_name, "
                        + "s.name AS service_name, a.appointment_date, a.appointment_time, a.status "
                        + "FROM appointments a "
                        + "JOIN users u ON a.user_id = u.id "
                        + "JOIN clinics c ON a.clinic_id = c.id "
                        + "JOIN services s ON a.service_id = s.id "
                        + "ORDER BY a.appointment_date, a.appointment_time";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /** Count appointments on a given date (yyyy-MM-dd). */
    public int countByAppointmentDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isoDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
