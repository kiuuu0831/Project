package com.cchc.admin.dao;

import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Service_quotas rows with labels for admin UI. */
public class AdminQuotaDAO {

    public List<ServiceQuotaRow> findAllRows() throws SQLException {
        List<ServiceQuotaRow> list = new ArrayList<>();
        String sql =
                "SELECT sq.id, sq.clinic_id, c.name AS clinic_name, sq.service_id, s.name AS service_name, "
                        + "sq.max_per_day, sq.max_per_timeslot, sq.cancellation_cutoff_hours, sq.max_active_bookings_per_patient "
                        + "FROM service_quotas sq "
                        + "JOIN clinics c ON sq.clinic_id = c.id "
                        + "JOIN services s ON sq.service_id = s.id "
                        + "ORDER BY c.name, s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ServiceQuotaRow r = new ServiceQuotaRow();
                r.setId(rs.getInt("id"));
                r.setClinicId(rs.getInt("clinic_id"));
                r.setClinicName(rs.getString("clinic_name"));
                r.setServiceId(rs.getInt("service_id"));
                r.setServiceName(rs.getString("service_name"));
                r.setMaxPerDay(rs.getInt("max_per_day"));
                r.setMaxPerTimeslot(rs.getInt("max_per_timeslot"));
                r.setCancellationCutoffHours(rs.getInt("cancellation_cutoff_hours"));
                r.setMaxActiveBookingsPerPatient(rs.getInt("max_active_bookings_per_patient"));
                list.add(r);
            }
        }
        return list;
    }

    public List<ServiceQuotaRow> findRowsByClinicId(int clinicId) throws SQLException {
        List<ServiceQuotaRow> list = new ArrayList<>();
        String sql =
                "SELECT sq.id, sq.clinic_id, c.name AS clinic_name, sq.service_id, s.name AS service_name, "
                        + "sq.max_per_day, sq.max_per_timeslot, sq.cancellation_cutoff_hours, sq.max_active_bookings_per_patient "
                        + "FROM service_quotas sq "
                        + "JOIN clinics c ON sq.clinic_id = c.id "
                        + "JOIN services s ON sq.service_id = s.id "
                        + "WHERE sq.clinic_id = ? "
                        + "ORDER BY s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServiceQuotaRow r = new ServiceQuotaRow();
                    r.setId(rs.getInt("id"));
                    r.setClinicId(rs.getInt("clinic_id"));
                    r.setClinicName(rs.getString("clinic_name"));
                    r.setServiceId(rs.getInt("service_id"));
                    r.setServiceName(rs.getString("service_name"));
                    r.setMaxPerDay(rs.getInt("max_per_day"));
                    r.setMaxPerTimeslot(rs.getInt("max_per_timeslot"));
                    r.setCancellationCutoffHours(rs.getInt("cancellation_cutoff_hours"));
                    r.setMaxActiveBookingsPerPatient(rs.getInt("max_active_bookings_per_patient"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public ServiceQuotaRow findByClinicAndService(int clinicId, int serviceId) throws SQLException {
        String sql =
                "SELECT sq.id, sq.clinic_id, c.name AS clinic_name, sq.service_id, s.name AS service_name, "
                        + "sq.max_per_day, sq.max_per_timeslot, sq.cancellation_cutoff_hours, sq.max_active_bookings_per_patient "
                        + "FROM service_quotas sq "
                        + "JOIN clinics c ON sq.clinic_id = c.id "
                        + "JOIN services s ON sq.service_id = s.id "
                        + "WHERE sq.clinic_id = ? AND sq.service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicId);
            ps.setInt(2, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ServiceQuotaRow r = new ServiceQuotaRow();
                    r.setId(rs.getInt("id"));
                    r.setClinicId(rs.getInt("clinic_id"));
                    r.setClinicName(rs.getString("clinic_name"));
                    r.setServiceId(rs.getInt("service_id"));
                    r.setServiceName(rs.getString("service_name"));
                    r.setMaxPerDay(rs.getInt("max_per_day"));
                    r.setMaxPerTimeslot(rs.getInt("max_per_timeslot"));
                    r.setCancellationCutoffHours(rs.getInt("cancellation_cutoff_hours"));
                    r.setMaxActiveBookingsPerPatient(rs.getInt("max_active_bookings_per_patient"));
                    return r;
                }
            }
        }
        return null;
    }

    public void update(ServiceQuotaRow r) throws SQLException {
        String sql =
                "UPDATE service_quotas SET max_per_day=?, max_per_timeslot=?, cancellation_cutoff_hours=?, "
                        + "max_active_bookings_per_patient=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getMaxPerDay());
            ps.setInt(2, r.getMaxPerTimeslot());
            ps.setInt(3, r.getCancellationCutoffHours());
            ps.setInt(4, r.getMaxActiveBookingsPerPatient());
            ps.setInt(5, r.getId());
            ps.executeUpdate();
        }
    }

    public static final class ServiceQuotaRow {
        private int id;
        private int clinicId;
        private String clinicName;
        private int serviceId;
        private String serviceName;
        private int maxPerDay;
        private int maxPerTimeslot;
        private int cancellationCutoffHours;
        private int maxActiveBookingsPerPatient;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getClinicId() {
            return clinicId;
        }

        public void setClinicId(int clinicId) {
            this.clinicId = clinicId;
        }

        public String getClinicName() {
            return clinicName;
        }

        public void setClinicName(String clinicName) {
            this.clinicName = clinicName;
        }

        public int getServiceId() {
            return serviceId;
        }

        public void setServiceId(int serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getMaxPerDay() {
            return maxPerDay;
        }

        public void setMaxPerDay(int maxPerDay) {
            this.maxPerDay = maxPerDay;
        }

        public int getMaxPerTimeslot() {
            return maxPerTimeslot;
        }

        public void setMaxPerTimeslot(int maxPerTimeslot) {
            this.maxPerTimeslot = maxPerTimeslot;
        }

        public int getCancellationCutoffHours() {
            return cancellationCutoffHours;
        }

        public void setCancellationCutoffHours(int cancellationCutoffHours) {
            this.cancellationCutoffHours = cancellationCutoffHours;
        }

        public int getMaxActiveBookingsPerPatient() {
            return maxActiveBookingsPerPatient;
        }

        public void setMaxActiveBookingsPerPatient(int maxActiveBookingsPerPatient) {
            this.maxActiveBookingsPerPatient = maxActiveBookingsPerPatient;
        }
    }
}
