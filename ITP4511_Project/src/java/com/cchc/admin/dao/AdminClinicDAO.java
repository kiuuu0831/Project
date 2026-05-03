package com.cchc.admin.dao;

import com.cchc.model.Clinic;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminClinicDAO {

    public List<Clinic> findAll() throws SQLException {
        List<Clinic> list = new ArrayList<>();
        String sql = "SELECT id, name, address, phone, is_active FROM clinics ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clinic c = new Clinic();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                c.setActive(rs.getInt("is_active") == 1);
                list.add(c);
            }
        }
        return list;
    }

    /**
     * Active clinics that have at least one service_quotas row (for quota filter UI only).
     */
    public List<Clinic> findActiveClinicsWithQuotasOrderByName() throws SQLException {
        List<Clinic> list = new ArrayList<>();
        String sql =
                "SELECT DISTINCT c.id, c.name, c.address, c.phone, c.is_active "
                        + "FROM clinics c "
                        + "INNER JOIN service_quotas sq ON sq.clinic_id = c.id "
                        + "WHERE c.is_active = 1 "
                        + "ORDER BY c.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clinic c = new Clinic();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                c.setActive(rs.getInt("is_active") == 1);
                list.add(c);
            }
        }
        return list;
    }

    public Clinic findById(int id) throws SQLException {
        String sql = "SELECT id, name, address, phone, is_active FROM clinics WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Clinic c = new Clinic();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setAddress(rs.getString("address"));
                    c.setPhone(rs.getString("phone"));
                    c.setActive(rs.getInt("is_active") == 1);
                    return c;
                }
            }
        }
        return null;
    }

    public void insert(Clinic c) throws SQLException {
        String sql = "INSERT INTO clinics (name, address, phone, is_active) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, emptyToNull(c.getAddress()));
            ps.setString(3, emptyToNull(c.getPhone()));
            ps.setInt(4, c.isActive() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void update(Clinic c) throws SQLException {
        String sql = "UPDATE clinics SET name=?, address=?, phone=?, is_active=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, emptyToNull(c.getAddress()));
            ps.setString(3, emptyToNull(c.getPhone()));
            ps.setInt(4, c.isActive() ? 1 : 0);
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
