<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.*,com.cchc.model.QueueTicket" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<%
    String cp = request.getContextPath();
    String uri = request.getRequestURI();
    String statusFilter = (String) request.getAttribute("statusFilter");
    if (statusFilter == null) {
        statusFilter = "ALL";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Queue Management</title>
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
                <div class="page-header__titles">
                    <h2>Walk-in Queue Management</h2>
                    <p class="page-header__lead muted">Filter by status, then update each ticket. Changes apply immediately.</p>
                </div>
                <a class="link-back" href="<%= cp %>/StaffDashboardServlet">Back to Dashboard</a>
            </div>

            <form method="get" action="<%= cp %>/QueueServlet" class="queue-toolbar" aria-label="Filter queue by status">
                <div class="queue-toolbar__field">
                    <label for="qStatusFilter">Status</label>
                    <select name="statusFilter" id="qStatusFilter">
                        <option value="ALL" <%= "ALL".equals(statusFilter) ? "selected" : "" %>>All</option>
                        <option value="WAITING" <%= "WAITING".equals(statusFilter) ? "selected" : "" %>>WAITING</option>
                        <option value="CALLED" <%= "CALLED".equals(statusFilter) ? "selected" : "" %>>CALLED</option>
                        <option value="SKIPPED" <%= "SKIPPED".equals(statusFilter) ? "selected" : "" %>>SKIPPED</option>
                        <option value="COMPLETED" <%= "COMPLETED".equals(statusFilter) ? "selected" : "" %>>COMPLETED</option>
                        <option value="EXPIRED" <%= "EXPIRED".equals(statusFilter) ? "selected" : "" %>>EXPIRED</option>
                    </select>
                </div>
                <button type="submit" class="btn-admin btn-admin-primary">Apply filter</button>
            </form>

            <%
                List<QueueTicket> queueTickets =
                    (List<QueueTicket>) request.getAttribute("queueTickets");
                boolean hasRows = queueTickets != null && !queueTickets.isEmpty();
            %>
            <% if (!hasRows) { %>
            <div class="queue-empty" role="status">
                <p class="queue-empty__title">No queue tickets</p>
                <p class="queue-empty__hint">Try another status, or check back when patients join the walk-in queue.</p>
            </div>
            <% } else { %>
            <div class="table-wrap queue-table">
                <table>
                    <thead>
                    <tr>
                        <th scope="col">Queue #</th>
                        <th scope="col">Patient</th>
                        <th scope="col">Clinic</th>
                        <th scope="col">Service</th>
                        <th scope="col">Status</th>
                        <th scope="col">Wait</th>
                        <th scope="col">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (QueueTicket q : queueTickets) {
                            String cur = q.getStatus() != null ? q.getStatus().trim() : "";
                    %>
                    <tr>
                        <td><span class="queue-num"><%= q.getQueueNumber() %></span></td>
                        <td><strong><%= q.getPatientName() %></strong></td>
                        <td><%= q.getClinicName() %></td>
                        <td><%= q.getServiceName() %></td>
                        <td><cchc:statusBadge value="<%= q.getStatus() %>" /></td>
                        <td class="queue-wait"><%= q.getEstimatedWait() %></td>
                        <td class="queue-actions-cell">
                            <form class="queue-action-form" action="<%= cp %>/QueueServlet" method="post">
                                <input type="hidden" name="queueId" value="<%= q.getQueueId() %>">
                                <input type="hidden" name="statusFilter" value="<%= statusFilter %>">
                                <select name="status" aria-label="Set status for queue <%= q.getQueueNumber() %>">
                                    <option value="WAITING" <%= "WAITING".equalsIgnoreCase(cur) ? "selected" : "" %>>等候中 (WAITING)</option>
                                    <option value="CALLED" <%= "CALLED".equalsIgnoreCase(cur) ? "selected" : "" %>>已叫號 (CALLED)</option>
                                    <option value="SKIPPED" <%= "SKIPPED".equalsIgnoreCase(cur) ? "selected" : "" %>>已跳過 (SKIPPED)</option>
                                    <option value="COMPLETED" <%= "COMPLETED".equalsIgnoreCase(cur) ? "selected" : "" %>>已完成 (COMPLETED)</option>
                                    <option value="EXPIRED" <%= "EXPIRED".equalsIgnoreCase(cur) ? "selected" : "" %>>已過期 (EXPIRED)</option>
                                </select>
                                <button type="submit" class="btn-admin btn-admin-primary btn-admin-sm">Update</button>
                            </form>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </div>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
