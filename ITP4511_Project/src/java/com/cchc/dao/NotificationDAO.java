package com.cchc.dao;

import com.cchc.model.Notification;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void insert(int userId, String title, String message, String type) {
        if (title == null || message == null || type == null) {
            return;
        }

        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?,?,?,?,0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type.trim().toUpperCase());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();

        String sql =
            "SELECT n.id AS notification_id, u.username, n.title, n.message, n.type, n.is_read, n.created_at "
            + "FROM notifications n JOIN users u ON n.user_id = u.id "
            + "WHERE n.user_id = ? ORDER BY n.created_at DESC LIMIT 100";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setUsername(rs.getString("username"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setType(rs.getString("type"));
                    n.setIsRead(rs.getInt("is_read") == 1);
                    n.setCreatedAt(rs.getString("created_at"));
                    list.add(n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Notification> getNotificationsByUsername(String username) {
        List<Notification> list = new ArrayList<>();

        String sql =
            "SELECT n.id AS notification_id, u.username, n.title, n.message, n.type, n.is_read, n.created_at "
            + "FROM notifications n "
            + "JOIN users u ON n.user_id = u.id "
            + "WHERE u.username = ? ORDER BY n.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setUsername(rs.getString("username"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setType(rs.getString("type"));
                    n.setIsRead(rs.getInt("is_read") == 1);
                    n.setCreatedAt(rs.getString("created_at"));
                    list.add(n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}