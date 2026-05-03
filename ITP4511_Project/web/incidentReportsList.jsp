<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cchc.model.IncidentReport" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
    List<IncidentReport> rows = (List<IncidentReport>) request.getAttribute("incidentReports");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Incident reports log</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />

<div class="layout">
    <div class="sidebar">
        <p class="sidebar-heading">Staff menu</p>
        <a href="<%= cp %>/StaffDashboardServlet" class="sidebar-link<%= uri.contains("StaffDashboard") ? " active" : "" %>">Dashboard</a>
        <a href="<%= cp %>/AppointmentServlet" class="sidebar-link<%= uri.contains("AppointmentServlet") ? " active" : "" %>">Appointments</a>
        <a href="<%= cp %>/QueueServlet" class="sidebar-link<%= uri.contains("QueueServlet") ? " active" : "" %>">Queue</a>
        <a href="<%= cp %>/IncidentServlet" class="sidebar-link<%= uri.contains("IncidentServlet") && !uri.contains("IncidentList") ? " active" : "" %>">Incident report</a>
        <a href="<%= cp %>/IncidentListServlet" class="sidebar-link<%= uri.contains("IncidentList") || uri.contains("incidentReportsList") ? " active" : "" %>">Incident log</a>
        <a href="<%= cp %>/NotificationServlet" class="sidebar-link<%= uri.contains("NotificationServlet") ? " active" : "" %>">Notifications</a>
    </div>

    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2>Incident reports log</h2>
                <a class="link-back" href="<%= cp %>/IncidentServlet">New report</a>
            </div>
            <p class="muted">Read-only list from table <code>incident_reports</code> (newest first).</p>

            <% if (rows == null || rows.isEmpty()) { %>
            <p class="muted">No incident reports yet.</p>
            <% } else { %>
            <div class="table-wrap">
                <table>
                    <tr>
                        <th>ID</th>
                        <th>When</th>
                        <th>Clinic</th>
                        <th>Service</th>
                        <th>Type</th>
                        <th>Reported by</th>
                        <th>Description</th>
                    </tr>
                    <% for (IncidentReport r : rows) { %>
                    <tr>
                        <td><%= r.getIncidentId() %></td>
                        <td><%= r.getCreatedAt() != null ? r.getCreatedAt() : "—" %></td>
                        <td><%= r.getClinicName() != null ? r.getClinicName() : "—" %></td>
                        <td><%= r.getServiceName() != null ? r.getServiceName() : "—" %></td>
                        <td><%= r.getIssueType() != null ? r.getIssueType() : "—" %></td>
                        <td><%= r.getReportedBy() != null ? r.getReportedBy() : "—" %></td>
                        <td><%= r.getDescription() != null ? r.getDescription() : "—" %></td>
                    </tr>
                    <% } %>
                </table>
            </div>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
