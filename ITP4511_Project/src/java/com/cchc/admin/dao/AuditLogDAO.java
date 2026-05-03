package com.cchc.admin.dao;

import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public static final class AuditEntry {
        private int id;
        private String username;
        private String action;
        private String targetType;
        private Integer targetId;
        private String details;
        private String ipAddress;
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getAction() {
            return action;
        }

        public String getTargetType() {
            return targetType;
        }

        public Integer getTargetId() {
            return targetId;
        }

        public String getDetails() {
            return details;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public List<AuditEntry> search(String usernameLike, String actionLike, String dateFrom, String dateTo)
            throws SQLException {

        List<AuditEntry> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, u.username, a.action, a.target_type, a.target_id, a.details, a.ip_address, a.created_at "
                        + "FROM audit_logs a JOIN users u ON a.user_id = u.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (usernameLike != null && !usernameLike.isBlank()) {
            sql.append("AND u.username LIKE ? ");
            params.add("%" + usernameLike.trim() + "%");
        }
        if (actionLike != null && !actionLike.isBlank()) {
            sql.append("AND a.action LIKE ? ");
            params.add("%" + actionLike.trim() + "%");
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append("AND DATE(a.created_at) >= ? ");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append("AND DATE(a.created_at) <= ? ");
            params.add(dateTo);
        }
        sql.append("ORDER BY a.created_at DESC LIMIT 500");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditEntry e = new AuditEntry();
                    e.id = rs.getInt("id");
                    e.username = rs.getString("username");
                    e.action = rs.getString("action");
                    e.targetType = rs.getString("target_type");
                    int tid = rs.getInt("target_id");
                    e.targetId = rs.wasNull() ? null : tid;
                    e.details = rs.getString("details");
                    e.ipAddress = rs.getString("ip_address");
                    e.createdAt = rs.getString("created_at");
                    list.add(e);
                }
            }
        }
        return list;
    }
}
