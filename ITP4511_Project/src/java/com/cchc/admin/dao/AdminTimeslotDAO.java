package com.cchc.admin.dao;

import com.cchc.model.Timeslot;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminTimeslotDAO {

    public List<Timeslot> findRecent(int limit) throws SQLException {
        List<Timeslot> list = new ArrayList<>();
        String sql =
                "SELECT t.id, t.clinic_id, t.service_id, t.slot_date, t.start_time, t.end_time, "
                        + "t.max_capacity, t.current_booked, t.is_active, c.name AS clinic_name, s.name AS service_name "
                        + "FROM timeslots t "
                        + "JOIN clinics c ON t.clinic_id = c.id "
                        + "JOIN services s ON t.service_id = s.id "
                        + "ORDER BY t.slot_date DESC, t.start_time DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Timeslot findById(int id) throws SQLException {
        String sql =
                "SELECT t.id, t.clinic_id, t.service_id, t.slot_date, t.start_time, t.end_time, "
                        + "t.max_capacity, t.current_booked, t.is_active, c.name AS clinic_name, s.name AS service_name "
                        + "FROM timeslots t JOIN clinics c ON t.clinic_id = c.id "
                        + "JOIN services s ON t.service_id = s.id WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public void insert(Timeslot t) throws SQLException {
        String sql =
                "INSERT INTO timeslots (clinic_id, service_id, slot_date, start_time, end_time, max_capacity, "
                        + "current_booked, is_active) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getClinicId());
            ps.setInt(2, t.getServiceId());
            ps.setString(3, t.getSlotDate());
            ps.setString(4, t.getStartTime());
            ps.setString(5, t.getEndTime());
            ps.setInt(6, t.getMaxCapacity());
            ps.setInt(7, t.getCurrentBooked());
            ps.setInt(8, t.isActive() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void update(Timeslot t) throws SQLException {
        String sql =
                "UPDATE timeslots SET clinic_id=?, service_id=?, slot_date=?, start_time=?, end_time=?, "
                        + "max_capacity=?, current_booked=?, is_active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getClinicId());
            ps.setInt(2, t.getServiceId());
            ps.setString(3, t.getSlotDate());
            ps.setString(4, t.getStartTime());
            ps.setString(5, t.getEndTime());
            ps.setInt(6, t.getMaxCapacity());
            ps.setInt(7, t.getCurrentBooked());
            ps.setInt(8, t.isActive() ? 1 : 0);
            ps.setInt(9, t.getId());
            ps.executeUpdate();
        }
    }

    private static Timeslot map(ResultSet rs) throws SQLException {
        Timeslot t = new Timeslot();
        t.setId(rs.getInt("id"));
        t.setClinicId(rs.getInt("clinic_id"));
        t.setServiceId(rs.getInt("service_id"));
        t.setSlotDate(rs.getString("slot_date"));
        t.setStartTime(rs.getString("start_time"));
        t.setEndTime(rs.getString("end_time"));
        t.setMaxCapacity(rs.getInt("max_capacity"));
        t.setCurrentBooked(rs.getInt("current_booked"));
        t.setActive(rs.getInt("is_active") == 1);
        t.setClinicNameDisplay(rs.getString("clinic_name"));
        t.setServiceNameDisplay(rs.getString("service_name"));
        return t;
    }
}
