package com.cchc.patient.dao;

import com.cchc.dao.NotificationDAO;
import com.cchc.patient.model.PatientQueueTicketView;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Same-day walk-in queue for patients. */
public class PatientQueueDAO {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public int joinQueueToday(int userId, int clinicId, int serviceId) throws BookingException, SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!clinicServiceActive(conn, clinicId, serviceId)) {
                    throw new BookingException("Clinic or service is not available.");
                }
                if (hasActiveQueueToday(conn, userId, clinicId, serviceId)) {
                    throw new BookingException("You already have an active queue ticket for this clinic and service today.");
                }

                int nextNum = nextQueueNumber(conn, clinicId, serviceId);
                int qid = insertTicket(conn, userId, clinicId, serviceId, nextNum);

                conn.commit();

                notificationDAO.insert(
                    userId,
                    "Queue joined",
                    "Your queue number is " + nextNum + ". Please wait at the clinic.",
                    "QUEUE"
                );

                notifyStaff(
                    "New queue ticket",
                    "A patient joined the queue. Queue number: " + nextNum + ".",
                    "QUEUE"
                );

                return qid;
            } catch (BookingException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<PatientQueueTicketView> listMyTicketsRecent(int userId, int limit) throws SQLException {
        List<PatientQueueTicketView> list = new ArrayList<>();
        String sql =
            "SELECT q.id, q.queue_number, c.name AS clinic_name, s.name AS service_name, q.status, "
            + "q.joined_at, q.estimated_wait_minutes "
            + "FROM queue_tickets q "
            + "JOIN clinics c ON q.clinic_id = c.id "
            + "JOIN services s ON q.service_id = s.id "
            + "WHERE q.user_id = ? "
            + "ORDER BY q.joined_at DESC "
            + "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientQueueTicketView v = new PatientQueueTicketView();
                    v.setId(rs.getInt("id"));
                    v.setQueueNumber(rs.getInt("queue_number"));
                    v.setClinicName(rs.getString("clinic_name"));
                    v.setServiceName(rs.getString("service_name"));
                    v.setStatus(rs.getString("status"));
                    v.setJoinedAt(rs.getString("joined_at"));
                    int w = rs.getInt("estimated_wait_minutes");
                    v.setEstimatedWait(rs.wasNull() ? null : w);
                    list.add(v);
                }
            }
        }
        return list;
    }

    public int countActiveQueueToday(int userId) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM queue_tickets WHERE user_id = ? AND DATE(joined_at) = CURDATE() "
            + "AND status IN ('WAITING','CALLED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private boolean clinicServiceActive(Connection conn, int clinicId, int serviceId) throws SQLException {
        String sql =
            "SELECT 1 FROM clinics c JOIN services s ON s.clinic_id = c.id "
            + "WHERE c.id = ? AND s.id = ? AND c.is_active = 1 AND s.is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasActiveQueueToday(Connection conn, int userId, int clinicId, int serviceId)
    throws SQLException {
        String sql =
            "SELECT 1 FROM queue_tickets WHERE user_id = ? AND clinic_id = ? AND service_id = ? "
            + "AND DATE(joined_at) = CURDATE() AND status IN ('WAITING','CALLED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, clinicId);
            ps.setInt(3, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int nextQueueNumber(Connection conn, int clinicId, int serviceId) throws SQLException {
        String sql =
            "SELECT COALESCE(MAX(queue_number), 0) + 1 AS n FROM queue_tickets "
            + "WHERE clinic_id = ? AND service_id = ? AND DATE(joined_at) = CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("n") : 1;
            }
        }
    }

    private int insertTicket(Connection conn, int userId, int clinicId, int serviceId, int queueNumber)
    throws SQLException {
        String sql =
            "INSERT INTO queue_tickets (user_id, clinic_id, service_id, queue_number, status, estimated_wait_minutes) "
            + "VALUES (?,?,?,?,'WAITING',15)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, clinicId);
            ps.setInt(3, serviceId);
            ps.setInt(4, queueNumber);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    private Integer findAnyStaffUserId() {
        String sql = "SELECT id FROM users WHERE role = 'STAFF' ORDER BY id LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void notifyStaff(String title, String message, String type) {
        Integer staffUserId = findAnyStaffUserId();
        if (staffUserId != null) {
            notificationDAO.insert(staffUserId, title, message, type);
        }
    }
}