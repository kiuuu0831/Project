package com.cchc.admin.util;

import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.http.HttpServletRequest;

public final class AuditLogger {

    private AuditLogger() {
    }

    public static void record(
            HttpServletRequest req,
            int actorUserId,
            String action,
            String targetType,
            Integer targetId,
            String details) {

        String sql =
                "INSERT INTO audit_logs (user_id, action, target_type, target_id, details, ip_address) "
                        + "VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, actorUserId);
            ps.setString(2, truncate(action, 100));
            ps.setString(3, targetType != null ? truncate(targetType, 50) : null);
            if (targetId != null) {
                ps.setInt(4, targetId);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, details);
            ps.setString(6, req != null ? req.getRemoteAddr() : null);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
