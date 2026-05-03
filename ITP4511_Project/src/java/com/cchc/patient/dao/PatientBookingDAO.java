package com.cchc.patient.dao;

import com.cchc.admin.dao.AdminQuotaDAO;
import com.cchc.admin.dao.PolicyDAO;
import com.cchc.dao.NotificationDAO;
import com.cchc.patient.model.PatientAppointmentView;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Patient appointment booking with service_quotas checks and timeslots capacity.
 */
public class PatientBookingDAO {

    private final AdminQuotaDAO quotaDAO = new AdminQuotaDAO();
    private final PolicyDAO policyDAO = new PolicyDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<PatientAppointmentView> listAppointments(int userId) throws SQLException {
        List<PatientAppointmentView> list = new ArrayList<>();
        String sql =
            "SELECT a.id, a.timeslot_id, c.name AS clinic_name, s.name AS service_name, "
            + "a.appointment_date, a.appointment_time, a.status, a.clinic_id, a.service_id "
            + "FROM appointments a "
            + "JOIN clinics c ON a.clinic_id = c.id "
            + "JOIN services s ON a.service_id = s.id "
            + "WHERE a.user_id = ? "
            + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientAppointmentView v = new PatientAppointmentView();
                    v.setId(rs.getInt("id"));
                    v.setClinicId(rs.getInt("clinic_id"));
                    v.setServiceId(rs.getInt("service_id"));
                    int tid = rs.getInt("timeslot_id");
                    v.setTimeslotId(rs.wasNull() ? null : tid);
                    v.setClinicName(rs.getString("clinic_name"));
                    v.setServiceName(rs.getString("service_name"));
                    v.setAppointmentDate(rs.getString("appointment_date"));
                    v.setAppointmentTime(rs.getString("appointment_time"));
                    String st = rs.getString("status");
                    v.setStatus(st);
                    v.setCanModify(computeCanModify(
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        st,
                        v.getClinicId(),
                        v.getServiceId()));
                    list.add(v);
                }
            }
        }
        return list;
    }

    private boolean computeCanModify(String dateStr, String timeStr, String status, int clinicId, int serviceId) {
        if (status == null || (!"PENDING".equalsIgnoreCase(status) && !"CONFIRMED".equalsIgnoreCase(status))) {
            return false;
        }
        try {
            int cutoff = resolveCutoffHours(clinicId, serviceId);
            LocalDateTime appt = LocalDateTime.parse(dateStr + "T" + timeStr.trim());
            return LocalDateTime.now().plusHours(cutoff).isBefore(appt);
        } catch (Exception e) {
            return false;
        }
    }

    private int resolveCutoffHours(int clinicId, int serviceId) throws SQLException {
        AdminQuotaDAO.ServiceQuotaRow q = quotaDAO.findByClinicAndService(clinicId, serviceId);
        if (q != null && q.getCancellationCutoffHours() > 0) {
            return q.getCancellationCutoffHours();
        }
        String v = policyDAO.findValueByKey("cancellation_cutoff_hours");
        if (v != null) {
            try {
                return Integer.parseInt(v.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 24;
    }

    public int bookAppointment(int userId, int timeslotId) throws BookingException, SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                SlotRow slot = lockSlot(conn, timeslotId);
                if (slot == null || !slot.active) {
                    throw new BookingException("Timeslot is not available.");
                }

                AdminQuotaDAO.ServiceQuotaRow quota = quotaDAO.findByClinicAndService(slot.clinicId, slot.serviceId);
                if (quota == null) {
                    throw new BookingException("This service has no quota configuration; booking is disabled.");
                }

                if (slot.currentBooked >= slot.effectiveMax) {
                    throw new BookingException("This timeslot is full.");
                }

                int dayCount = countDayBookings(conn, slot.clinicId, slot.serviceId, slot.slotDate, null);
                if (dayCount >= quota.getMaxPerDay()) {
                    throw new BookingException("Daily booking limit for this service has been reached.");
                }

                int active = countActivePatientBookings(conn, userId);
                if (active >= quota.getMaxActiveBookingsPerPatient()) {
                    throw new BookingException("You have reached the maximum number of active bookings.");
                }

                String globalMax = policyDAO.findValueByKey("max_active_bookings");
                if (globalMax != null) {
                    try {
                        int g = Integer.parseInt(globalMax.trim());
                        if (active >= g) {
                            throw new BookingException("You have reached the maximum number of active bookings (policy).");
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                int apptId = insertAppointment(
                    conn,
                    userId,
                    slot.clinicId,
                    slot.serviceId,
                    timeslotId,
                    slot.slotDate,
                    slot.startTime
                );

                bumpSlotBooked(conn, timeslotId, 1);
                conn.commit();

                notificationDAO.insert(
                    userId,
                    "Appointment booked",
                    "Your appointment on " + slot.slotDate + " " + slot.startTime + " is pending confirmation.",
                    "APPOINTMENT"
                );

                notifyStaff(
                    "New appointment booking",
                    "A patient booked an appointment on " + slot.slotDate + " " + slot.startTime + ".",
                    "APPOINTMENT"
                );

                return apptId;
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

    public void cancelAppointment(int userId, int appointmentId) throws BookingException, SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ApptRow ap = lockAppointment(conn, appointmentId);
                if (ap == null || ap.userId != userId) {
                    throw new BookingException("Appointment not found.");
                }

                if (!"PENDING".equalsIgnoreCase(ap.status) && !"CONFIRMED".equalsIgnoreCase(ap.status)) {
                    throw new BookingException("This appointment cannot be cancelled.");
                }

                int cutoff = resolveCutoffHours(ap.clinicId, ap.serviceId);
                LocalDateTime appt = LocalDateTime.parse(ap.date + "T" + ap.time.trim());
                if (!LocalDateTime.now().plusHours(cutoff).isBefore(appt)) {
                    throw new BookingException("Cancellation is no longer allowed (within cutoff window).");
                }

                updateStatus(conn, appointmentId, "CANCELLED");
                if (ap.timeslotId != null) {
                    bumpSlotBooked(conn, ap.timeslotId, -1);
                }

                conn.commit();

                notificationDAO.insert(
                    userId,
                    "Appointment cancelled",
                    "Your appointment on " + ap.date + " has been cancelled.",
                    "APPOINTMENT"
                );

                notifyStaff(
                    "Appointment cancelled",
                    "A patient cancelled an appointment on " + ap.date + " " + ap.time + ".",
                    "APPOINTMENT"
                );
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

    public void rescheduleAppointment(int userId, int appointmentId, int newTimeslotId)
    throws BookingException, SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ApptRow ap = lockAppointment(conn, appointmentId);
                if (ap == null || ap.userId != userId) {
                    throw new BookingException("Appointment not found.");
                }

                if (!"PENDING".equalsIgnoreCase(ap.status) && !"CONFIRMED".equalsIgnoreCase(ap.status)) {
                    throw new BookingException("This appointment cannot be rescheduled.");
                }

                int cutoff = resolveCutoffHours(ap.clinicId, ap.serviceId);
                LocalDateTime appt = LocalDateTime.parse(ap.date + "T" + ap.time.trim());
                if (!LocalDateTime.now().plusHours(cutoff).isBefore(appt)) {
                    throw new BookingException("Reschedule is no longer allowed (within cutoff window).");
                }

                SlotRow newSlot = lockSlot(conn, newTimeslotId);
                if (newSlot == null || !newSlot.active) {
                    throw new BookingException("New timeslot is not available.");
                }

                if (newSlot.clinicId != ap.clinicId || newSlot.serviceId != ap.serviceId) {
                    throw new BookingException("New slot must be for the same clinic and service.");
                }

                AdminQuotaDAO.ServiceQuotaRow quota = quotaDAO.findByClinicAndService(newSlot.clinicId, newSlot.serviceId);
                if (quota == null) {
                    throw new BookingException("Quota configuration missing.");
                }

                if (newSlot.currentBooked >= newSlot.effectiveMax) {
                    throw new BookingException("Selected timeslot is full.");
                }

                int dayCount = countDayBookings(conn, newSlot.clinicId, newSlot.serviceId, newSlot.slotDate, appointmentId);
                if (!newSlot.slotDate.equals(ap.date) && dayCount >= quota.getMaxPerDay()) {
                    throw new BookingException("Daily limit reached for the new date.");
                }

                if (ap.timeslotId != null && ap.timeslotId.equals(newTimeslotId)) {
                    throw new BookingException("Already booked in this timeslot.");
                }

                if (ap.timeslotId != null) {
                    bumpSlotBooked(conn, ap.timeslotId, -1);
                }

                bumpSlotBooked(conn, newTimeslotId, 1);
                updateAppointmentSlot(conn, appointmentId, newTimeslotId, newSlot.slotDate, newSlot.startTime);

                conn.commit();

                notificationDAO.insert(
                    userId,
                    "Appointment rescheduled",
                    "Your appointment was moved to " + newSlot.slotDate + " " + newSlot.startTime + ".",
                    "APPOINTMENT"
                );

                notifyStaff(
                    "Appointment rescheduled",
                    "A patient rescheduled an appointment to " + newSlot.slotDate + " " + newSlot.startTime + ".",
                    "APPOINTMENT"
                );
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

    public Map<String, Integer> appointmentCountsByMonth(int userId, int months) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        String sql =
            "SELECT DATE_FORMAT(appointment_date, '%Y-%m') AS ym, COUNT(*) AS cnt "
            + "FROM appointments WHERE user_id = ? AND status NOT IN ('CANCELLED') "
            + "AND appointment_date >= ? "
            + "GROUP BY ym ORDER BY ym";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, start.format(DateTimeFormatter.ISO_LOCAL_DATE));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("ym"), rs.getInt("cnt"));
                }
            }
        }
        return map;
    }

    public int countUpcomingAppointments(int userId) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM appointments WHERE user_id = ? AND status IN ('PENDING','CONFIRMED') "
            + "AND (appointment_date > CURDATE() OR (appointment_date = CURDATE() AND appointment_time >= CURTIME()))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
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

    private static final class SlotRow {
        int clinicId;
        int serviceId;
        String slotDate;
        String startTime;
        int currentBooked;
        int effectiveMax;
        boolean active;
    }

    private static final class ApptRow {
        int userId;
        Integer timeslotId;
        String status;
        String date;
        String time;
        int clinicId;
        int serviceId;
    }

    private SlotRow lockSlot(Connection conn, int timeslotId) throws SQLException {
        String sql =
            "SELECT t.clinic_id, t.service_id, t.slot_date, t.start_time, t.current_booked, t.is_active, "
            + "LEAST(t.max_capacity, COALESCE(NULLIF(sq.max_per_timeslot, 0), t.max_capacity)) AS eff_max "
            + "FROM timeslots t "
            + "LEFT JOIN service_quotas sq ON sq.clinic_id = t.clinic_id AND sq.service_id = t.service_id "
            + "WHERE t.id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, timeslotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SlotRow s = new SlotRow();
                s.clinicId = rs.getInt("clinic_id");
                s.serviceId = rs.getInt("service_id");
                s.slotDate = rs.getString("slot_date");
                s.startTime = rs.getString("start_time");
                s.currentBooked = rs.getInt("current_booked");
                s.effectiveMax = rs.getInt("eff_max");
                s.active = rs.getInt("is_active") == 1;
                return s;
            }
        }
    }

    private ApptRow lockAppointment(Connection conn, int appointmentId) throws SQLException {
        String sql =
            "SELECT user_id, timeslot_id, status, appointment_date, appointment_time, clinic_id, service_id "
            + "FROM appointments WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ApptRow a = new ApptRow();
                a.userId = rs.getInt("user_id");
                int tid = rs.getInt("timeslot_id");
                a.timeslotId = rs.wasNull() ? null : tid;
                a.status = rs.getString("status");
                a.date = rs.getString("appointment_date");
                a.time = rs.getString("appointment_time");
                a.clinicId = rs.getInt("clinic_id");
                a.serviceId = rs.getInt("service_id");
                return a;
            }
        }
    }

    private int countDayBookings(Connection conn, int clinicId, int serviceId, String isoDate, Integer excludeAppointmentId)
    throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM appointments WHERE clinic_id = ? AND service_id = ? AND appointment_date = ? "
            + "AND status NOT IN ('CANCELLED')";
        if (excludeAppointmentId != null) {
            sql += " AND id <> ?";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.setString(3, isoDate);
            if (excludeAppointmentId != null) {
                ps.setInt(4, excludeAppointmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int countActivePatientBookings(Connection conn, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE user_id = ? AND status IN ('PENDING','CONFIRMED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insertAppointment(
        Connection conn, int userId, int clinicId, int serviceId, int timeslotId, String date, String time)
    throws SQLException {
        String sql =
            "INSERT INTO appointments (user_id, clinic_id, service_id, timeslot_id, appointment_date, appointment_time, status) "
            + "VALUES (?,?,?,?,?,?,'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, clinicId);
            ps.setInt(3, serviceId);
            ps.setInt(4, timeslotId);
            ps.setString(5, date);
            ps.setString(6, time);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    private void bumpSlotBooked(Connection conn, int timeslotId, int delta) throws SQLException {
        String sql = "UPDATE timeslots SET current_booked = current_booked + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, timeslotId);
            ps.executeUpdate();
        }
    }

    private void updateStatus(Connection conn, int appointmentId, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        }
    }

    private void updateAppointmentSlot(Connection conn, int appointmentId, int timeslotId, String date, String time)
    throws SQLException {
        String sql = "UPDATE appointments SET timeslot_id = ?, appointment_date = ?, appointment_time = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, timeslotId);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setInt(4, appointmentId);
            ps.executeUpdate();
        }
    }
}