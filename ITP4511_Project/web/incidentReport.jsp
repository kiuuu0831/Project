<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Incident Report</title>
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
        <div class="container narrow-form">
            <div class="page-header">
                <h2>Report Operational Issue</h2>
                <a class="link-back" href="<%= cp %>/StaffDashboardServlet">Back to Dashboard</a>
            </div>
            <p class="muted" style="margin-top:0;"><a href="<%= cp %>/IncidentListServlet">View incident reports log</a> (read-only)</p>

            <form action="<%= cp %>/IncidentServlet" method="post">
                <label for="clinicName">Clinic Name</label>
                <input id="clinicName" type="text" name="clinicName" required>

                <label for="serviceName">Service Name</label>
                <input id="serviceName" type="text" name="serviceName" required>

                <label for="issueType">Issue Type</label>
                <input id="issueType" type="text" name="issueType" required>

                <label for="description">Description</label>
                <textarea id="description" name="description" rows="5" required></textarea>

                <button type="submit">Submit Incident</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
