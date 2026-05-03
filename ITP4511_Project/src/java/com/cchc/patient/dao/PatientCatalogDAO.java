package com.cchc.patient.dao;

import com.cchc.model.Clinic;
import com.cchc.model.Service;
import com.cchc.model.Timeslot;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Read-only catalog for patients (clinics, services, available timeslots). */
public class PatientCatalogDAO {

    public List<Clinic> listActiveClinics() throws SQLException {
        List<Clinic> list = new ArrayList<>();
        String sql = "SELECT id, name, address, phone, is_active FROM clinics WHERE is_active = 1 ORDER BY name";
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
     * Active clinics that have at least one active service (walk-in queue and other flows should not
     * list empty clinics).
     */
    public List<Clinic> listActiveClinicsWithActiveServices() throws SQLException {
        List<Clinic> list = new ArrayList<>();
        String sql =
                "SELECT DISTINCT c.id, c.name, c.address, c.phone, c.is_active "
                        + "FROM clinics c "
                        + "INNER JOIN services s ON s.clinic_id = c.id AND s.is_active = 1 "
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

    /** All active services at active clinics (e.g. walk-in queue picker); data comes only from DB. */
    public List<Service> listAllActiveServicesAtActiveClinics() throws SQLException {
        List<Service> list = new ArrayList<>();
        String sql =
                "SELECT s.id, s.clinic_id, s.name, s.description, s.duration_minutes, s.is_active, c.name AS clinic_name "
                        + "FROM services s JOIN clinics c ON s.clinic_id = c.id "
                        + "WHERE s.is_active = 1 AND c.is_active = 1 ORDER BY c.name, s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Service s = new Service();
                s.setId(rs.getInt("id"));
                s.setClinicId(rs.getInt("clinic_id"));
                s.setName(rs.getString("name"));
                s.setDescription(rs.getString("description"));
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                s.setActive(rs.getInt("is_active") == 1);
                s.setClinicNameDisplay(rs.getString("clinic_name"));
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Clinics the patient can book online: active clinic with at least one active service that has a
     * {@code service_quotas} row (required by booking rules).
     */
    public List<Clinic> listClinicsEligibleForBooking() throws SQLException {
        List<Clinic> list = new ArrayList<>();
        String sql =
                "SELECT DISTINCT c.id, c.name, c.address, c.phone, c.is_active "
                        + "FROM clinics c "
                        + "INNER JOIN services s ON s.clinic_id = c.id AND s.is_active = 1 "
                        + "INNER JOIN service_quotas sq ON sq.clinic_id = c.id AND sq.service_id = s.id "
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

    public List<Service> listActiveServicesForClinic(int clinicId) throws SQLException {
        List<Service> list = new ArrayList<>();
        String sql =
                "SELECT s.id, s.clinic_id, s.name, s.description, s.duration_minutes, s.is_active, c.name AS clinic_name "
                        + "FROM services s "
                        + "JOIN clinics c ON s.clinic_id = c.id "
                        + "INNER JOIN service_quotas sq ON sq.clinic_id = s.clinic_id AND sq.service_id = s.id "
                        + "WHERE s.clinic_id = ? AND s.is_active = 1 AND c.is_active = 1 ORDER BY s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Service s = new Service();
                    s.setId(rs.getInt("id"));
                    s.setClinicId(rs.getInt("clinic_id"));
                    s.setName(rs.getString("name"));
                    s.setDescription(rs.getString("description"));
                    s.setDurationMinutes(rs.getInt("duration_minutes"));
                    s.setActive(rs.getInt("is_active") == 1);
                    s.setClinicNameDisplay(rs.getString("clinic_name"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * Active timeslots with remaining capacity for booking UI.
     * {@code max_per_timeslot} from {@code service_quotas} caps effective capacity when lower than slot max.
     */
    public List<Timeslot> listBookableSlots(int clinicId, int serviceId, String isoDate) throws SQLException {
        List<Timeslot> list = new ArrayList<>();
        String sql =
                "SELECT t.id, t.clinic_id, t.service_id, t.slot_date, t.start_time, t.end_time, "
                        + "LEAST(t.max_capacity, COALESCE(NULLIF(sq.max_per_timeslot, 0), t.max_capacity)) AS eff_max, "
                        + "t.current_booked, t.is_active, c.name AS clinic_name, s.name AS service_name "
                        + "FROM timeslots t "
                        + "JOIN clinics c ON t.clinic_id = c.id "
                        + "JOIN services s ON t.service_id = s.id "
                        + "LEFT JOIN service_quotas sq ON sq.clinic_id = t.clinic_id AND sq.service_id = t.service_id "
                        + "WHERE t.clinic_id = ? AND t.service_id = ? AND t.slot_date = ? AND t.is_active = 1 "
                        + "AND c.is_active = 1 AND s.is_active = 1 "
                        + "AND t.current_booked < LEAST(t.max_capacity, COALESCE(NULLIF(sq.max_per_timeslot, 0), t.max_capacity)) "
                        + "ORDER BY t.start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.setString(3, isoDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timeslot t = new Timeslot();
                    t.setId(rs.getInt("id"));
                    t.setClinicId(rs.getInt("clinic_id"));
                    t.setServiceId(rs.getInt("service_id"));
                    t.setSlotDate(rs.getString("slot_date"));
                    t.setStartTime(rs.getString("start_time"));
                    t.setEndTime(rs.getString("end_time"));
                    t.setMaxCapacity(rs.getInt("eff_max"));
                    t.setCurrentBooked(rs.getInt("current_booked"));
                    t.setActive(rs.getInt("is_active") == 1);
                    t.setClinicNameDisplay(rs.getString("clinic_name"));
                    t.setServiceNameDisplay(rs.getString("service_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /** Bookable slots between two dates (inclusive), for reschedule picker. */
    public List<Timeslot> listBookableSlotsInRange(int clinicId, int serviceId, LocalDate from, LocalDate to)
            throws SQLException {
        List<Timeslot> list = new ArrayList<>();
        String sql =
                "SELECT t.id, t.clinic_id, t.service_id, t.slot_date, t.start_time, t.end_time, "
                        + "LEAST(t.max_capacity, COALESCE(NULLIF(sq.max_per_timeslot, 0), t.max_capacity)) AS eff_max, "
                        + "t.current_booked, t.is_active, c.name AS clinic_name, s.name AS service_name "
                        + "FROM timeslots t "
                        + "JOIN clinics c ON t.clinic_id = c.id "
                        + "JOIN services s ON t.service_id = s.id "
                        + "LEFT JOIN service_quotas sq ON sq.clinic_id = t.clinic_id AND sq.service_id = t.service_id "
                        + "WHERE t.clinic_id = ? AND t.service_id = ? AND t.slot_date BETWEEN ? AND ? AND t.is_active = 1 "
                        + "AND c.is_active = 1 AND s.is_active = 1 "
                        + "AND t.current_booked < LEAST(t.max_capacity, COALESCE(NULLIF(sq.max_per_timeslot, 0), t.max_capacity)) "
                        + "ORDER BY t.slot_date, t.start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            ps.setString(3, from.format(DateTimeFormatter.ISO_LOCAL_DATE));
            ps.setString(4, to.format(DateTimeFormatter.ISO_LOCAL_DATE));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timeslot t = new Timeslot();
                    t.setId(rs.getInt("id"));
                    t.setClinicId(rs.getInt("clinic_id"));
                    t.setServiceId(rs.getInt("service_id"));
                    t.setSlotDate(rs.getString("slot_date"));
                    t.setStartTime(rs.getString("start_time"));
                    t.setEndTime(rs.getString("end_time"));
                    t.setMaxCapacity(rs.getInt("eff_max"));
                    t.setCurrentBooked(rs.getInt("current_booked"));
                    t.setActive(rs.getInt("is_active") == 1);
                    t.setClinicNameDisplay(rs.getString("clinic_name"));
                    t.setServiceNameDisplay(rs.getString("service_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public Timeslot findTimeslotById(Connection conn, int timeslotId) throws SQLException {
        String sql =
                "SELECT t.id, t.clinic_id, t.service_id, t.slot_date, t.start_time, t.end_time, "
                        + "t.max_capacity, t.current_booked, t.is_active, c.name AS clinic_name, s.name AS service_name "
                        + "FROM timeslots t JOIN clinics c ON t.clinic_id = c.id "
                        + "JOIN services s ON t.service_id = s.id WHERE t.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, timeslotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSlot(rs);
                }
            }
        }
        return null;
    }

    private static Timeslot mapSlot(ResultSet rs) throws SQLException {
        Timeslot t = new Timeslot();
        t.setId(rs.getInt("id"));
        t.setClinicId(rs.getInt("clinic_id"));
        t.setServiceId(rs.getInt("service_id"));
        t.setSlotDate(rs.getString("slot_date"));
        t.setStartTime(rs.getString("start_time"));
        t.setEndTime(rs.getString("end_time"));
        t.setMaxCapacity(rs.getInt("max_capacity"));
        t.setCurrentBooked(rs.getInt("current_booked"));
        t.setActive(rs.getInt("is_active") == 1);
        t.setClinicNameDisplay(rs.getString("clinic_name"));
        t.setServiceNameDisplay(rs.getString("service_name"));
        return t;
    }
}
