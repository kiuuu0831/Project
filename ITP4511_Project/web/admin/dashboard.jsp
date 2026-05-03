<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.admin.model.DashboardStats" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    DashboardStats stats = (DashboardStats) request.getAttribute("stats");
    String dashError = (String) request.getAttribute("dashError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<jsp:include page="/header.jsp" />

<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />

    <div class="main-content">
        <div class="container">
            <h2>Administrator Dashboard</h2>
            <p class="muted">Signed in as <strong><%= admin.getFullName() != null ? admin.getFullName() : admin.getUsername() %></strong></p>

            <% if (dashError != null) { %>
                <p class="error"><%= dashError %></p>
            <% } %>

            <% if (stats != null) { %>
            <div class="cards">
                <div class="card">
                    <h3><%= stats.getTodayAppointmentCount() %></h3>
                    <p>Today's appointments</p>
                </div>
                <div class="card">
                    <h3><%= stats.getWaitingQueueCount() %></h3>
                    <p>Waiting in queue</p>
                </div>
                <div class="card">
                    <h3><%= String.format("%.1f", stats.getUtilizationPercent()) %>%</h3>
                    <p>Today's slot utilization</p>
                </div>
                <div class="card">
                    <h3><%= String.format("%.1f", stats.getNoShowRatePercent()) %>%</h3>
                    <p>No-show rate (30 days)</p>
                </div>
            </div>
            <% } %>
        </div>
    </div>
</div>

</body>
</html>
