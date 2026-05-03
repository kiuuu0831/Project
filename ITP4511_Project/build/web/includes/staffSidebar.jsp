<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
%>
<div class="sidebar">
    <p class="sidebar-heading">職員選單</p>
    <a href="<%= cp %>/StaffDashboardServlet" class="sidebar-link<%= uri.contains("StaffDashboard") ? " active" : "" %>">Dashboard</a>
    <a href="<%= cp %>/AppointmentServlet" class="sidebar-link<%= uri.contains("AppointmentServlet") ? " active" : "" %>">Appointments</a>
    <a href="<%= cp %>/QueueServlet" class="sidebar-link<%= uri.contains("QueueServlet") ? " active" : "" %>">Queue</a>
    <a href="<%= cp %>/IncidentServlet" class="sidebar-link<%= uri.contains("IncidentServlet") && !uri.contains("IncidentList") || uri.contains("incidentReport") ? " active" : "" %>">Incident report</a>
    <a href="<%= cp %>/IncidentListServlet" class="sidebar-link<%= uri.contains("IncidentList") || uri.contains("incidentReportsList") ? " active" : "" %>">Incident log</a>
    <a href="<%= cp %>/NotificationServlet" class="sidebar-link<%= uri.contains("NotificationServlet") ? " active" : "" %>">Notifications</a>
</div>
