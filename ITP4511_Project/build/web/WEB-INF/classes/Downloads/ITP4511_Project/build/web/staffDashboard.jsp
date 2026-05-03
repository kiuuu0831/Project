<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.cchc.model.Notification" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Staff Dashboard</title>
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
                <h2>Clinic Staff Dashboard</h2>
            </div>

            <div class="cards">
                <div class="card">
                    <h3>${appointmentCount}</h3>
                    <p>Appointments Today</p>
                </div>
                <div class="card">
                    <h3>${queueCount}</h3>
                    <p>Queue Tickets Today</p>
                </div>
            </div>

            <h3>Recent Notifications</h3>
            <ul class="notice-list">
                <%
                    List<Notification> notifications =
                        (List<Notification>) request.getAttribute("notifications");
                    if (notifications != null) {
                        for (Notification n : notifications) {
                            String title = n.getTitle();
                            if (title == null || title.isBlank()) {
                                title = "—";
                            }
                %>
                    <li>
                        <strong><%= title %></strong>
                        <span class="muted">[<%= n.getType() %>]</span>
                        — <%= n.getMessage() %>
                    </li>
                <%
                        }
                    }
                %>
            </ul>
        </div>
    </div>
</div>

</body>
</html>
