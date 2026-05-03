<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.cchc.patient.model.PatientAppointmentView" %>
<%@ page import="com.cchc.model.Timeslot" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="cchc" uri="/WEB-INF/cchc.tld" %>
<%
    String cp = request.getContextPath();
    List<PatientAppointmentView> appointments = (List<PatientAppointmentView>) request.getAttribute("appointments");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My appointments</title>
    <link rel="stylesheet" href="<%= cp %>/css/style.css">
</head>
<body>
<div class="layout" style="display:block; min-height:100vh;">
    <div class="main-content" style="padding-top:1rem;">
        <div class="container">
            <%@ include file="nav.jsp" %>

            <h2>My appointments</h2>
            <p class="muted">Cancel or reschedule within the policy window.</p>

            <% if (appointments == null || appointments.isEmpty()) { %>
            <p class="muted">You have no bookings yet.</p>
            <% } else { %>
            <div class="table-wrap">
                <table>
                    <tr>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Clinic</th>
                        <th>Service</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    <% for (PatientAppointmentView v : appointments) { %>
                    <tr>
                        <td><%= v.getAppointmentDate() %></td>
                        <td><%= v.getAppointmentTime() %></td>
                        <td><%= v.getClinicName() %></td>
                        <td><%= v.getServiceName() %></td>
                        <td><cchc:statusBadge value="<%= v.getStatus() %>"/></td>
                        <td>
                            <% if (v.isCanModify()) { %>
                            <form action="<%= cp %>/patient/PatientAppointmentServlet" method="post" style="margin:0 0 0.5rem 0;">
                                <input type="hidden" name="action" value="cancel">
                                <input type="hidden" name="appointmentId" value="<%= v.getId() %>">
                                <button type="submit" class="btn-secondary" onclick="return confirm('Cancel this appointment?');">Cancel</button>
                            </form>
                            <% 
                            boolean hasReslot = false;
                            if (v.getRescheduleOptions() != null) {
                                for (Timeslot t : v.getRescheduleOptions()) {
                                    if (v.getTimeslotId() == null || t.getId() != v.getTimeslotId()) {
                                        hasReslot = true;
                                        break;
                                    }
                                }
                            }
                            %>
                            <% if (hasReslot) { %>
                            <form action="<%= cp %>/patient/PatientAppointmentServlet" method="post" class="quota-clinic-filter" style="margin:0;">
                                <input type="hidden" name="action" value="reschedule">
                                <input type="hidden" name="appointmentId" value="<%= v.getId() %>">
                                <label>New slot</label>
                                <select name="newTimeslotId" required>
                                    <% for (Timeslot t : v.getRescheduleOptions()) {
                                        if (v.getTimeslotId() != null && t.getId() == v.getTimeslotId()) {
                                            continue;
                                        } %>
                                    <option value="<%= t.getId() %>"><%= t.getStartTime() %> – <%= t.getEndTime() %> (<%= Math.max(0, t.getMaxCapacity() - t.getCurrentBooked()) %> left)</option>
                                    <% } %>
                                </select>
                                <button type="submit">Reschedule</button>
                            </form>
                            <% } %>
                            <% } else { %>
                            <span class="muted">—</span>
                            <% } %>
                        </td>
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
