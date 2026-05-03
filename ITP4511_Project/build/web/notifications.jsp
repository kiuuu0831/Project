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
    <title>Notifications</title>
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
                <h2>Staff Notifications</h2>
                <a class="link-back" href="<%= cp %>/StaffDashboardServlet">Back to Dashboard</a>
            </div>

            <div class="container narrow-form" style="margin-bottom:1.25rem;padding:1rem 1.15rem;background:var(--surface-2);border:1px solid var(--border);border-radius:var(--radius-sm);">
                <h3 style="margin:0 0 0.5rem;font-size:1rem;">About this page (notifications update)</h3>
                <ul class="muted" style="margin:0;padding-left:1.25rem;font-size:0.9rem;line-height:1.5;">
                    <li>Messages are loaded from the <strong>notifications</strong> table for <strong>your staff user</strong> (matched by username).</li>
                    <li>The patient portal now writes notifications when patients <strong>book / cancel / reschedule</strong> appointments and <strong>join the walk-in queue</strong> (same table, different <code>user_id</code>).</li>
                    <li>Staff only see rows created for their account; to receive operational alerts they must be inserted for that staff user in the database (or future admin tooling).</li>
                    <li>Types include <code>APPOINTMENT</code>, <code>QUEUE</code>, <code>SYSTEM</code>, <code>REMINDER</code> (see column <code>type</code>).</li>
                </ul>
            </div>

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
                        <span class="muted">(<%= n.getCreatedAt() %>)</span>
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
