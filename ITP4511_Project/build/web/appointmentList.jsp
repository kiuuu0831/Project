<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.cchc.model.Appointment" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Appointments</title>
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
        <a href="<%= cp %>/IncidentServlet" class="sidebar-link<%= uri.contains("IncidentServlet") && !uri.contains("IncidentList") || uri.contains("incidentReport") ? " active" : "" %>">Incident report</a>
        <a href="<%= cp %>/IncidentListServlet" class="sidebar-link<%= uri.contains("IncidentList") || uri.contains("incidentReportsList") ? " active" : "" %>">Incident log</a>
        <a href="<%= cp %>/NotificationServlet" class="sidebar-link<%= uri.contains("NotificationServlet") ? " active" : "" %>">Notifications</a>
    </div>

    <div class="main-content">
        <div class="container">
            <div class="page-header">
                <h2>Daily Appointment List</h2>
                <a class="link-back" href="<%= cp %>/StaffDashboardServlet">Back to Dashboard</a>
            </div>

            <div class="table-wrap">
                <table>
                    <tr>
                        <th>ID</th>
                        <th>Patient</th>
                        <th>Clinic</th>
                        <th>Service</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>

                    <%
                        List<Appointment> appointments =
                            (List<Appointment>) request.getAttribute("appointments");

                        if (appointments != null) {
                            for (Appointment a : appointments) {
                                String cur = a.getStatus() != null ? a.getStatus().trim() : "";
                    %>
                    <tr>
                        <td><%= a.getAppointmentId() %></td>
                        <td><%= a.getPatientName() %></td>
                        <td><%= a.getClinicName() %></td>
                        <td><%= a.getServiceName() %></td>
                        <td><%= a.getAppointmentDate() %></td>
                        <td><%= a.getAppointmentTime() %></td>
                        <td><cchc:statusBadge value="<%= a.getStatus() %>" /></td>
                        <td>
                            <form action="<%= cp %>/AppointmentServlet" method="post">
                                <input type="hidden" name="appointmentId" value="<%= a.getAppointmentId() %>">
                                <select name="status">
                                    <option value="PENDING" <%= "PENDING".equalsIgnoreCase(cur) ? "selected" : "" %>>待處理 (PENDING)</option>
                                    <option value="CONFIRMED" <%= "CONFIRMED".equalsIgnoreCase(cur) ? "selected" : "" %>>已確認 (CONFIRMED)</option>
                                    <option value="COMPLETED" <%= "COMPLETED".equalsIgnoreCase(cur) ? "selected" : "" %>>已完成 (COMPLETED)</option>
                                    <option value="CANCELLED" <%= "CANCELLED".equalsIgnoreCase(cur) ? "selected" : "" %>>已取消 (CANCELLED)</option>
                                    <option value="NO_SHOW" <%= "NO_SHOW".equalsIgnoreCase(cur) ? "selected" : "" %>>缺席 (NO_SHOW)</option>
                                </select>
                                <button type="submit">Update</button>
                            </form>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
