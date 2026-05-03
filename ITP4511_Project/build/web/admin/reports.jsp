<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Appointment" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<Appointment> appts = (List<Appointment>) request.getAttribute("appointments");
    Map<String, Integer> statusCounts = (Map<String, Integer>) request.getAttribute("statusCounts");
    String df = (String) request.getAttribute("dateFrom");
    String dt = (String) request.getAttribute("dateTo");
    String st = (String) request.getAttribute("statusFilter");
    Integer totalAppts = (Integer) request.getAttribute("totalAppts");
    Integer noShowCount = (Integer) request.getAttribute("noShowCount");
    Double noShowRate = (Double) request.getAttribute("noShowRate");
    Integer slotBookedSum = (Integer) request.getAttribute("slotBookedSum");
    Integer slotCapacitySum = (Integer) request.getAttribute("slotCapacitySum");
    Integer slotFreeSum = (Integer) request.getAttribute("slotFreeSum");
    Double utilizationPercent = (Double) request.getAttribute("utilizationPercent");
    String reportError = (String) request.getAttribute("reportError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Reports</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Reporting &amp; analytics</h2>
            <% if (reportError != null) { %><p class="error"><%= reportError %></p><% } %>

            <form method="get" action="<%= request.getContextPath() %>/admin/AdminReportServlet" class="container" style="margin-bottom:20px;">
                <label>From</label>
                <input type="date" name="dateFrom" lang="en" title="YYYY-MM-DD" value="<%= df != null ? df : "" %>">
                <label>To</label>
                <input type="date" name="dateTo" lang="en" title="YYYY-MM-DD" value="<%= dt != null ? dt : "" %>">
                <label>Status</label>
                <select name="status">
                    <option value="ALL" <%= "ALL".equals(st) ? "selected" : "" %>>All</option>
                    <option value="PENDING" <%= "PENDING".equals(st) ? "selected" : "" %>>PENDING</option>
                    <option value="CONFIRMED" <%= "CONFIRMED".equals(st) ? "selected" : "" %>>CONFIRMED</option>
                    <option value="COMPLETED" <%= "COMPLETED".equals(st) ? "selected" : "" %>>COMPLETED</option>
                    <option value="CANCELLED" <%= "CANCELLED".equals(st) ? "selected" : "" %>>CANCELLED</option>
                    <option value="NO_SHOW" <%= "NO_SHOW".equals(st) ? "selected" : "" %>>NO_SHOW</option>
                </select>
                <button type="submit" class="btn-admin btn-admin-primary">Apply</button>
            </form>

            <% if (totalAppts != null && noShowCount != null && noShowRate != null) { %>
            <p><strong>Total appointments (range):</strong> <%= totalAppts %> &nbsp;|&nbsp; <strong>No-show:</strong> <%= noShowCount %> (<%= String.format("%.1f", noShowRate) %>%)</p>
            <% } %>

            <% if (utilizationPercent != null && slotBookedSum != null && slotCapacitySum != null && slotFreeSum != null) { %>
            <p><strong>Timeslot utilization (active slots in date range):</strong> <%= String.format("%.1f", utilizationPercent) %>%
                &mdash; booked <%= slotBookedSum %> / capacity <%= slotCapacitySum %>
            </p>
            <% } %>

            <div class="report-charts">
            <% if (statusCounts != null && !statusCounts.isEmpty()) { %>
            <div class="chart-box">
                <canvas id="statusBarChart"></canvas>
            </div>
            <div class="chart-box">
                <canvas id="statusPieChart"></canvas>
            </div>
            <script>
                (function() {
                    const labels = [
                        <% for (Map.Entry<String, Integer> e : statusCounts.entrySet()) { %>
                        '<%= e.getKey().replace("'", "\\'") %>',
                        <% } %>
                    ];
                    const values = [
                        <% for (Map.Entry<String, Integer> e : statusCounts.entrySet()) { %>
                        <%= e.getValue() %>,
                        <% } %>
                    ];
                    const colors = ['#1e3a5f','#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6'];
                    new Chart(document.getElementById('statusBarChart'), {
                        type: 'bar',
                        data: {
                            labels: labels,
                            datasets: [{
                                label: 'Appointments by status',
                                data: values,
                                backgroundColor: '#1e3a5f'
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true,
                            aspectRatio: 1.4,
                            plugins: { title: { display: true, text: 'Appointments by status (bar)' } },
                            scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
                        }
                    });
                    new Chart(document.getElementById('statusPieChart'), {
                        type: 'pie',
                        data: {
                            labels: labels,
                            datasets: [{
                                data: values,
                                backgroundColor: labels.map((_, i) => colors[i % colors.length])
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: true,
                            aspectRatio: 1,
                            plugins: { title: { display: true, text: 'Share by status (pie)' } }
                        }
                    });
                })();
            </script>
            <% } %>

            <% if (totalAppts != null && noShowCount != null && totalAppts > 0) { %>
            <div class="chart-box">
                <canvas id="noShowPieChart"></canvas>
            </div>
            <script>
                new Chart(document.getElementById('noShowPieChart'), {
                    type: 'pie',
                    data: {
                        labels: ['No-show', 'Other statuses'],
                        datasets: [{
                            data: [<%= noShowCount.intValue() %>, <%= Math.max(0, totalAppts.intValue() - noShowCount.intValue()) %>],
                            backgroundColor: ['#ef4444', '#94a3b8']
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: true,
                        aspectRatio: 1,
                        plugins: { title: { display: true, text: 'No-show vs other (appointments in range)' } }
                    }
                });
            </script>
            <% } %>

            <% if (slotBookedSum != null && slotFreeSum != null && (slotBookedSum + slotFreeSum) > 0) { %>
            <div class="chart-box">
                <canvas id="utilDoughnutChart"></canvas>
            </div>
            <script>
                new Chart(document.getElementById('utilDoughnutChart'), {
                    type: 'doughnut',
                    data: {
                        labels: ['Booked seats', 'Remaining capacity'],
                        datasets: [{
                            data: [<%= slotBookedSum %>, <%= slotFreeSum %>],
                            backgroundColor: ['#1e3a5f', '#e2e8f0']
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: true,
                        aspectRatio: 1,
                        plugins: { title: { display: true, text: 'Timeslot seat utilization (doughnut)' } }
                    }
                });
            </script>
            <% } %>
            </div>

            <h3>Appointment records</h3>
            <table>
                <tr><th>ID</th><th>Patient</th><th>Clinic</th><th>Service</th><th>Date</th><th>Time</th><th>Status</th></tr>
                <% if (appts != null) { for (Appointment a : appts) { %>
                <tr>
                    <td><%= a.getAppointmentId() %></td>
                    <td><%= a.getPatientName() %></td>
                    <td><%= a.getClinicName() %></td>
                    <td><%= a.getServiceName() %></td>
                    <td><%= a.getAppointmentDate() %></td>
                    <td><%= a.getAppointmentTime() %></td>
                    <td><%= a.getStatus() %></td>
                </tr>
                <% } } %>
            </table>
        </div>
    </div>
</div>
</body>
</html>
