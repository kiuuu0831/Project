package com.cchc.admin.dao;

import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAO {

    public static final class PolicyRow {
        private int id;
        private String policyKey;
        private String policyValue;
        private String description;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPolicyKey() {
            return policyKey;
        }

        public void setPolicyKey(String policyKey) {
            this.policyKey = policyKey;
        }

        public String getPolicyValue() {
            return policyValue;
        }

        public void setPolicyValue(String policyValue) {
            this.policyValue = policyValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public List<PolicyRow> findAll() throws SQLException {
        List<PolicyRow> list = new ArrayList<>();
        String sql = "SELECT id, policy_key, policy_value, description FROM policies ORDER BY policy_key";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PolicyRow p = new PolicyRow();
                p.setId(rs.getInt("id"));
                p.setPolicyKey(rs.getString("policy_key"));
                p.setPolicyValue(rs.getString("policy_value"));
                p.setDescription(rs.getString("description"));
                list.add(p);
            }
        }
        return list;
    }

    public String findValueByKey(String policyKey) throws SQLException {
        if (policyKey == null || policyKey.isBlank()) {
            return null;
        }
        String sql = "SELECT policy_value FROM policies WHERE policy_key = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, policyKey.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("policy_value");
                }
            }
        }
        return null;
    }

    public void updateValue(int policyId, String value, int updatedByUserId) throws SQLException {
        String sql = "UPDATE policies SET policy_value = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, updatedByUserId);
            ps.setInt(3, policyId);
            ps.executeUpdate();
        }
    }
}
