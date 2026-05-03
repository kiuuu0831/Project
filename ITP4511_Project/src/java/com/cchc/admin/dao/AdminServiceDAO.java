package com.cchc.admin.dao;

import com.cchc.model.Service;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminServiceDAO {

    public List<Service> findAllWithClinic() throws SQLException {
        List<Service> list = new ArrayList<>();
        String sql =
                "SELECT s.id, s.clinic_id, s.name, s.description, s.duration_minutes, s.is_active, c.name AS clinic_name "
                        + "FROM services s JOIN clinics c ON s.clinic_id = c.id ORDER BY c.name, s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapService(rs));
            }
        }
        return list;
    }

    public Service findById(int id) throws SQLException {
        String sql =
                "SELECT s.id, s.clinic_id, s.name, s.description, s.duration_minutes, s.is_active, c.name AS clinic_name "
                        + "FROM services s JOIN clinics c ON s.clinic_id = c.id WHERE s.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapService(rs);
                }
            }
        }
        return null;
    }

    public int insert(Service s) throws SQLException {
        String sql =
                "INSERT INTO services (clinic_id, name, description, duration_minutes, is_active) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getClinicId());
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getDescription());
            ps.setInt(4, s.getDurationMinutes());
            ps.setInt(5, s.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void ensureQuotaRow(int clinicId, int serviceId) throws SQLException {
        String sql = "INSERT IGNORE INTO service_quotas (clinic_id, service_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.executeUpdate();
        }
    }

    /**
     * Backfills {@code service_quotas} for every row in {@code services} that is missing one.
     * Older services created before {@link #ensureQuotaRow} need this so quota UI lists them.
     */
    public void ensureQuotaRowsForAllServices() throws SQLException {
        String sql =
                "INSERT IGNORE INTO service_quotas (clinic_id, service_id) "
                        + "SELECT s.clinic_id, s.id FROM services s";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public void update(Service s) throws SQLException {
        String sql =
                "UPDATE services SET clinic_id=?, name=?, description=?, duration_minutes=?, is_active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getClinicId());
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getDescription());
            ps.setInt(4, s.getDurationMinutes());
            ps.setInt(5, s.isActive() ? 1 : 0);
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        }
    }

    private static Service mapService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setClinicId(rs.getInt("clinic_id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setActive(rs.getInt("is_active") == 1);
        s.setClinicNameDisplay(rs.getString("clinic_name"));
        return s;
    }
}
