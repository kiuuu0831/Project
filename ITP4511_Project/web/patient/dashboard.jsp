<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.patient.model.PatientDashboardStats" %>
<%@ page import="com.cchc.patient.model.PatientAppointmentView" %>
<%@ page import="com.cchc.patient.model.PatientQueueTicketView" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<jsp:useBean id="dashStats" type="com.cchc.patient.model.PatientDashboardStats" scope="request"/>
<%
    String cp = request.getContextPath();
    List<PatientAppointmentView> upcoming = (List<PatientAppointmentView>) request.getAttribute("upcomingAppointments");
    List<PatientQueueTicketView> queues = (List<PatientQueueTicketView>) request.getAttribute("queueTickets");
    int[] chartCounts = dashStats.getMonthCounts();
    String[] chartLabels = dashStats.getMonthLabels();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Patient Dashboard</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js" defer></script>
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>Welcome back</h2>
            <p class="muted">Upcoming appointments: <strong><%= dashStats.getUpcomingCount() %></strong>
                · Active queue tickets today: <strong><%= dashStats.getActiveQueueCount() %></strong></p>

            <div class="cards" style="margin-top:1rem;">
                <div class="card">
                    <h3><%= dashStats.getUpcomingCount() %></h3>
                    <p>Upcoming bookings</p>
                </div>
                <div class="card">
                    <h3><%= dashStats.getActiveQueueCount() %></h3>
                    <p>In queue today</p>
                </div>
            </div>

            <h3>Appointment activity (last 6 months)</h3>
            <div style="max-width:520px;margin:1rem 0;">
                <canvas id="patientChart" height="200"></canvas>
            </div>
            <script>
                document.addEventListener('DOMContentLoaded', function () {
                    const el = document.getElementById('patientChart');
                    if (!el || typeof Chart === 'undefined') return;
                    const labels = [
                        <% for (int i = 0; chartLabels != null && i < chartLabels.length; i++) {
                            String lb = chartLabels[i].replace("\\", "\\\\").replace("'", "\\'");
                        %>'<%= lb %>'<%= i < chartLabels.length - 1 ? "," : "" %><% } %>
                    ];
                    const data = [
                        <% if (chartCounts != null) {
                            for (int i = 0; i < chartCounts.length; i++) {
                        %><%= chartCounts[i] %><%= i < chartCounts.length - 1 ? "," : "" %><% } } %>
                    ];
                    new Chart(el, {
                        type: 'bar',
                        data: {
                            labels: labels,
                            datasets: [{
                                label: 'Bookings (non-cancelled)',
                                data: data,
                                backgroundColor: 'rgba(13, 92, 92, 0.55)'
                            }]
                        },
                        options: {
                            scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
                            plugins: { legend: { display: false } }
                        }
                    });
                });
            </script>

            <h3>Next appointments</h3>
            <% if (upcoming == null || upcoming.isEmpty()) { %>
            <p class="muted">No upcoming bookings.</p>
            <% } else { %>
            <div class="table-wrap">
                <table>
                    <tr><th>Date</th><th>Time</th><th>Clinic</th><th>Service</th><th>Status</th></tr>
                    <% for (PatientAppointmentView a : upcoming) { %>
                    <tr>
                        <td><%= a.getAppointmentDate() %></td>
                        <td><%= a.getAppointmentTime() %></td>
                        <td><%= a.getClinicName() %></td>
                        <td><%= a.getServiceName() %></td>
                        <td><cchc:statusBadge value="<%= a.getStatus() %>"/></td>
                    </tr>
                    <% } %>
                </table>
            </div>
            <% } %>

            <h3>Recent queue tickets</h3>
            <% if (queues == null || queues.isEmpty()) { %>
            <p class="muted">No queue history yet.</p>
            <% } else { %>
            <div class="table-wrap">
                <table>
                    <tr><th>#</th><th>Clinic</th><th>Service</th><th>Status</th><th>Joined</th></tr>
                    <% for (PatientQueueTicketView q : queues) { %>
                    <tr>
                        <td><%= q.getQueueNumber() %></td>
                        <td><%= q.getClinicName() %></td>
                        <td><%= q.getServiceName() %></td>
                        <td><cchc:statusBadge value="<%= q.getStatus() %>"/></td>
                        <td><%= q.getJoinedAt() %></td>
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
