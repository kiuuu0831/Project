package com.cchc.admin.dao;

import com.cchc.model.User;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDAO {

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql =
                "SELECT u.id, u.username, u.password, u.full_name, u.email, u.phone, u.role, u.clinic_id, u.is_active, "
                        + "c.name AS clinic_display_name "
                        + "FROM users u LEFT JOIN clinics c ON u.clinic_id = c.id "
                        + "ORDER BY u.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public User findById(int id) throws SQLException {
        String sql =
                "SELECT u.id, u.username, u.password, u.full_name, u.email, u.phone, u.role, u.clinic_id, u.is_active, "
                        + "c.name AS clinic_display_name "
                        + "FROM users u LEFT JOIN clinics c ON u.clinic_id = c.id "
                        + "WHERE u.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int insert(User u) throws SQLException {
        String sql =
                "INSERT INTO users (username, password, full_name, email, phone, role, clinic_id, is_active) "
                        + "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername().trim());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName().trim());
            ps.setString(4, emptyToNull(u.getEmail()));
            ps.setString(5, emptyToNull(u.getPhone()));
            ps.setString(6, normalizeRole(u.getRole()));
            if (u.getClinicId() != null) {
                ps.setInt(7, u.getClinicId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, u.isActive() ? 1 : 0);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(User u, boolean updatePassword) throws SQLException {
        String sql;
        if (updatePassword) {
            sql =
                    "UPDATE users SET username=?, password=?, full_name=?, email=?, phone=?, role=?, clinic_id=?, is_active=? "
                            + "WHERE id=?";
        } else {
            sql =
                    "UPDATE users SET username=?, full_name=?, email=?, phone=?, role=?, clinic_id=?, is_active=? "
                            + "WHERE id=?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, u.getUsername().trim());
            if (updatePassword) {
                ps.setString(i++, u.getPassword());
            }
            ps.setString(i++, u.getFullName().trim());
            ps.setString(i++, emptyToNull(u.getEmail()));
            ps.setString(i++, emptyToNull(u.getPhone()));
            ps.setString(i++, normalizeRole(u.getRole()));
            if (u.getClinicId() != null) {
                ps.setInt(i++, u.getClinicId());
            } else {
                ps.setNull(i++, java.sql.Types.INTEGER);
            }
            ps.setInt(i++, u.isActive() ? 1 : 0);
            ps.setInt(i, u.getUserId());

            ps.executeUpdate();
        }
    }

    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        int cid = rs.getInt("clinic_id");
        u.setClinicId(rs.wasNull() ? null : cid);
        u.setActive(rs.getInt("is_active") == 1);
        u.setClinicDisplayName(rs.getString("clinic_display_name"));
        return u;
    }

    private static String emptyToNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s.trim();
    }

    /** Maps UI role to MySQL ENUM literal. */
    public static String normalizeRole(String role) {
        if (role == null) {
            return "PATIENT";
        }
        String r = role.trim().toUpperCase();
        if ("ADMIN".equals(r) || "STAFF".equals(r) || "PATIENT".equals(r)) {
            return r;
        }
        return "PATIENT";
    }
}
