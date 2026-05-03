package com.cchc.dao;

import com.cchc.model.QueueTicket;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QueueDAO {

    public List<QueueTicket> getAllQueueTickets() {
        return getQueueTicketsByStatus(null);
    }

    /**
     * @param statusExact {@code queue_tickets.status} value (e.g. WAITING), or null / blank to list all
     */
    public List<QueueTicket> getQueueTicketsByStatus(String statusExact) {
        List<QueueTicket> list = new ArrayList<>();
        String sql =
                "SELECT q.id AS queue_id, u.full_name AS patient_name, c.name AS clinic_name, "
                        + "s.name AS service_name, q.queue_number, q.status, "
                        + "q.estimated_wait_minutes AS estimated_wait "
                        + "FROM queue_tickets q "
                        + "JOIN users u ON q.user_id = u.id "
                        + "JOIN clinics c ON q.clinic_id = c.id "
                        + "JOIN services s ON q.service_id = s.id ";
        boolean filter =
                statusExact != null
                        && !statusExact.isBlank()
                        && !"ALL".equalsIgnoreCase(statusExact.trim());
        if (filter) {
            sql += "WHERE q.status = ? ";
        }
        sql += "ORDER BY q.joined_at DESC, q.queue_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                ps.setString(1, statusExact.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QueueTicket q = new QueueTicket();
                    q.setQueueId(rs.getInt("queue_id"));
                    q.setPatientName(rs.getString("patient_name"));
                    q.setClinicName(rs.getString("clinic_name"));
                    q.setServiceName(rs.getString("service_name"));
                    q.setQueueNumber(rs.getInt("queue_number"));
                    q.setStatus(rs.getString("status"));
                    int wait = rs.getInt("estimated_wait");
                    q.setEstimatedWait(rs.wasNull() ? "" : String.valueOf(wait));
                    list.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /** Queue tickets created today (by {@code joined_at} date). */
    public int countJoinedToday() {
        String sql = "SELECT COUNT(*) FROM queue_tickets WHERE DATE(joined_at) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateQueueStatus(int queueId, String status) {
        String sql = "UPDATE queue_tickets SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, queueId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
