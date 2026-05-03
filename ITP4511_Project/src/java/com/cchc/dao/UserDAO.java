package com.cchc.dao;

import com.cchc.model.User;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class UserDAO {

    public User login(String username, String password) {
        return login(username, password, "staff");
    }

    /** Patient portal login ({@code role = PATIENT}). */
    public User loginAsPatient(String username, String password) {
        return login(username, password, "patient");
    }

    /**
     * Administrator login: plain-text password must match DB; role must be admin or administrator (case-insensitive).
     */
    public User loginForAdministrator(String username, String password) {
        User user = null;

        if (username == null || password == null) {
            return null;
        }

        // cchc_admin.users.role ENUM: PATIENT, STAFF, ADMIN — compare as ENUM literal (avoid LOWER(ENUM) quirks).
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'ADMIN'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password.trim());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Admin login success - user found");
                user = mapUser(rs);
            } else {
                System.out.println("Admin login failed - no matching record (check username, password, role in users)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public User login(String username, String password, String expectedRole) {
        User user = null;

        if (username == null || password == null || expectedRole == null) {
            return null;
        }

        String roleEnum = toSchemaRoleEnum(expectedRole);
        if (roleEnum == null) {
            return null;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password.trim());
            ps.setString(3, roleEnum);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Login success - user found");
                user = mapUser(rs);
            } else {
                System.out.println("Login failed - no matching record");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    /** Maps login hint to {@code users.role} ENUM value in cchc_admin.sql. */
    private static String toSchemaRoleEnum(String expectedRole) {
        if (expectedRole == null) {
            return null;
        }
        return switch (expectedRole.trim().toUpperCase(Locale.ROOT)) {
            case "STAFF" -> "STAFF";
            case "ADMIN", "ADMINISTRATOR" -> "ADMIN";
            case "PATIENT" -> "PATIENT";
            default -> null;
        };
    }

    public boolean isUsernameTaken(String username) {
        if (username == null || username.isBlank()) {
            return true;
        }
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * @return new user id, or {@code -1} if insert failed (e.g. duplicate username)
     */
    public int registerPatient(String username, String password, String fullName, String email, String phone) {
        String sql =
                "INSERT INTO users (username, password, full_name, email, phone, role, clinic_id, is_active) "
                        + "VALUES (?,?,?,?,?,'PATIENT',NULL,1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, fullName.trim());
            ps.setString(4, email != null && !email.isBlank() ? email.trim() : null);
            ps.setString(5, phone != null && !phone.isBlank() ? phone.trim() : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updatePatientProfile(int userId, String fullName, String email, String phone) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ? AND role = 'PATIENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, email != null && !email.isBlank() ? email.trim() : null);
            ps.setString(3, phone != null && !phone.isBlank() ? phone.trim() : null);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    public void updatePasswordForPatient(int userId, String newPasswordPlain) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND role = 'PATIENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordPlain);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Maps a users row. PK may be {@code user_id}, {@code id}, or {@code userid}; other fields use common aliases.
     */
    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(firstIntColumn(rs, "user_id", "id", "userid"));
        user.setUsername(requireStringColumn(rs, "username", "user_name", "login"));
        user.setPassword(requireStringColumn(rs, "password", "passwd", "user_password"));
        user.setFullName(firstStringColumn(rs, "full_name", "fullname", "name"));
        user.setRole(requireStringColumn(rs, "role", "user_role"));
        user.setEmail(firstStringColumn(rs, "email"));
        user.setPhone(firstStringColumn(rs, "phone"));
        user.setClinicId(firstNullableInt(rs, "clinic_id"));
        Boolean active = readTinyIntAsBoolean(rs, "is_active");
        if (active != null) {
            user.setActive(active);
        }
        return user;
    }

    private static Integer firstNullableInt(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (md.getColumnLabel(i).equalsIgnoreCase(columnName)) {
                int v = rs.getInt(i);
                return rs.wasNull() ? null : v;
            }
        }
        return null;
    }

    private static Boolean readTinyIntAsBoolean(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (md.getColumnLabel(i).equalsIgnoreCase(columnName)) {
                return rs.getInt(i) == 1;
            }
        }
        return null;
    }

    private static int firstIntColumn(ResultSet rs, String... columnNames) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i).toLowerCase(Locale.ROOT);
            for (String n : columnNames) {
                if (label.equals(n.toLowerCase(Locale.ROOT))) {
                    return rs.getInt(i);
                }
            }
        }
        return 0;
    }

    private static String firstStringColumn(ResultSet rs, String... columnNames) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i).toLowerCase(Locale.ROOT);
            for (String n : columnNames) {
                if (label.equals(n.toLowerCase(Locale.ROOT))) {
                    return rs.getString(i);
                }
            }
        }
        return null;
    }

    private static String requireStringColumn(ResultSet rs, String... columnNames) throws SQLException {
        String v = firstStringColumn(rs, columnNames);
        if (v == null) {
            throw new SQLException("Missing column (one of): " + String.join(", ", columnNames));
        }
        return v;
    }
}
