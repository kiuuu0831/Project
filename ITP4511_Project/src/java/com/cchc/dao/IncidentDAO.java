package com.cchc.dao;

import com.cchc.model.IncidentReport;
import com.cchc.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    /** Recent rows from {@code incident_reports} (newest first). */
    public List<IncidentReport> findRecent(int limit) {
        List<IncidentReport> list = new ArrayList<>();
        if (limit <= 0) {
            limit = 100;
        }
        String sql =
                "SELECT incident_id, clinic_name, service_name, issue_type, description, reported_by, created_at "
                        + "FROM incident_reports ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IncidentReport r = new IncidentReport();
                    r.setIncidentId(rs.getInt("incident_id"));
                    r.setClinicName(rs.getString("clinic_name"));
                    r.setServiceName(rs.getString("service_name"));
                    r.setIssueType(rs.getString("issue_type"));
                    r.setDescription(rs.getString("description"));
                    r.setReportedBy(rs.getString("reported_by"));
                    r.setCreatedAt(rs.getString("created_at"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addIncident(IncidentReport incident) {
        String sql = "INSERT INTO incident_reports (clinic_name, service_name, issue_type, description, reported_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, incident.getClinicName());
            ps.setString(2, incident.getServiceName());
            ps.setString(3, incident.getIssueType());
            ps.setString(4, incident.getDescription());
            ps.setString(5, incident.getReportedBy());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}