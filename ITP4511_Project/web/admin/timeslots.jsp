<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.model.User" %>
<%@ page import="com.cchc.model.Timeslot" %>
<%@ page import="java.util.List" %>
<%
    User admin = (User) session.getAttribute("adminUser");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/adminLogin.jsp");
        return;
    }
    List<Timeslot> slots = (List<Timeslot>) request.getAttribute("timeslots");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Timeslots</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="/header.jsp" />
<div class="layout">
    <jsp:include page="includes/sidebar.jsp" />
    <div class="main-content">
        <div class="container">
            <h2>Timeslots</h2>
            <% if (request.getAttribute("timeslotError") != null) { %>
            <p class="error"><%= request.getAttribute("timeslotError") %></p>
            <% } %>
            <div class="admin-actions">
                <a class="btn-admin btn-admin-primary" href="<%= request.getContextPath() %>/admin/AdminTimeslotServlet?action=new">Add timeslot</a>
            </div>
            <table>
                <tr>
                    <th>ID</th><th>Date</th><th>Start</th><th>End</th><th>Clinic</th><th>Service</th>
                    <th>Booked / Max</th><th>Active</th><th></th>
                </tr>
                <% if (slots != null) { for (Timeslot t : slots) { %>
                <tr>
                    <td><%= t.getId() %></td>
                    <td><%= t.getSlotDate() %></td>
                    <td><%= t.getStartTime() %></td>
                    <td><%= t.getEndTime() %></td>
                    <td><%= t.getClinicNameDisplay() %></td>
                    <td><%= t.getServiceNameDisplay() %></td>
                    <td><%= t.getCurrentBooked() %> / <%= t.getMaxCapacity() %></td>
                    <td><%= t.isActive() ? "Yes" : "No" %></td>
                    <td><a class="btn-admin btn-admin-sm btn-admin-ghost" href="<%= request.getContextPath() %>/admin/AdminTimeslotServlet?action=edit&id=<%= t.getId() %>">Edit</a></td>
                </tr>
                <% } } %>
            </table>
        </div>
    </div>
</div>
</body>
</html>
